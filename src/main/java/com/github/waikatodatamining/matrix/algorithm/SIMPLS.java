/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * SIMPLS.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * SIMPLS algorithm.
 * <br>
 * See here:
 * <a href="http://www.statsoft.com/Textbook/Partial-Least-Squares#SIMPLS">SIMPLS (StatSoft)</a>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SIMPLS
  extends AbstractSingleResponsePLS {

  private static final long serialVersionUID = 4899661745515419256L;

  /** the number of coefficients in W to keep (0 keep all). */
  protected int m_NumCoefficients;

  /** the W matrix */
  protected Matrix m_W;

  /** the B matrix (used for prediction) */
  protected Matrix m_B;

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    setNumCoefficients(0);
  }

  /**
   * Resets the member variables.
   */
  @Override
  protected void reset() {
    super.reset();

    m_B = null;
    m_W = null;
  }

  /**
   * Sets the number of coefficients of W matrix to keep (rest gets zeroed).
   *
   * @param value 	the number of coefficients, 0 to keep all
   */
  public void setNumCoefficients(int value) {
    m_NumCoefficients = value;
    reset();
  }

  /**
   * returns the number of coefficients of W matrix to keep (rest gets zeroed).
   *
   * @return 		the maximum number of attributes, 0 to keep all
   */
  public int getNumCoefficients() {
    return m_NumCoefficients;
  }

  /**
   * Returns the all the available matrices.
   *
   * @return		the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "W",
      "B"
    };
  }

  /**
   * Returns the matrix with the specified name.
   *
   * @param name	the name of the matrix
   * @return		the matrix, null if not available
   */
  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      case "W":
	return m_W;
      case "B":
	return m_B;
      default:
	return null;
    }
  }

  /**
   * Whether the algorithm supports return of loadings.
   *
   * @return		true if supported
   * @see		#getLoadings()
   */
  public boolean hasLoadings() {
    return true;
  }

  /**
   * Returns the loadings, if available.
   *
   * @return		the loadings, null if not available
   */
  public Matrix getLoadings() {
    return getMatrix("W");
  }

  /**
   * Zeroes the coefficients of the W matrix beyond the specified number of
   * coefficients.
   *
   * @param in		the matrix to process in-place
   */
  protected void slim(Matrix in) {
    double[][] B = in.toRawCopy2D();

    for (int i = 0; i < in.numColumns(); i++) {
      Matrix l = in.getSubMatrix(0, in.numRows() - 1, i, i);
      double[] ld = l.toRawCopy1D();
      for (int t = 0; t < ld.length; t++) {
	ld[t] = Math.abs(ld[t]);
      }
      int[] srt = Utils.sort(ld);
      //int index = srt.length - 1 - srt[Math.min(getNumCoefficients(),srt.length-1)]; //nonono
      int index = srt[Math.max(srt.length - 1 - getNumCoefficients(), 0)];

      double val = ld[index];
      for (int c = 0; c < in.numRows(); c++) {
	if (Math.abs(B[c][i]) < val) {
	  B[c][i] = 0;
	}
      }
    }
  }

  /**
   * Initializes using the provided data.
   *
   * @param predictors the input data
   * @param response   the dependent variable(s)
   * @throws Exception if analysis fails
   * @return null if successful, otherwise error message
   */
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix A, A_trans;
    Matrix M;
    Matrix X_trans;
    Matrix C, c;
    Matrix Q, q;
    Matrix W, w;
    Matrix P, p, p_trans;
    Matrix v, v_trans;
    int h;

    X_trans = predictors.transpose();
    A = X_trans.mul(response);
    M = X_trans.mul(predictors);
    C = Matrix.identity(predictors.numColumns(), predictors.numColumns());
    W = new Matrix(predictors.numColumns(), getNumComponents());
    P = new Matrix(predictors.numColumns(), getNumComponents());
    Q = new Matrix(1, getNumComponents());

    for (h = 0; h < getNumComponents(); h++) {
      // 1. qh as dominant EigenVector of Ah'*Ah
      A_trans = A.transpose();
      q = MatrixHelper.getDominantEigenVector(A_trans.mul(A));

      // 2. wh=Ah*qh, ch=wh'*Mh*wh, wh=wh/sqrt(ch), store wh in W as column
      w = A.mul(q);
      c = w.transpose().mul(M).mul(w);
      w = w.mul(1.0 / StrictMath.sqrt(c.asDouble()));
      W.setColumn(h, w);

      // 3. ph=Mh*wh, store ph in P as column
      p = M.mul(w);
      p_trans = p.transpose();
      P.setColumn(h, p);

      // 4. qh=Ah'*wh, store qh in Q as column
      q = A_trans.mul(w);
      Q.setColumn(h, q);

      // 5. vh=Ch*ph, vh=vh/||vh||
      v = C.mul(p);
      MatrixHelper.normalizeVector(v);
      v_trans = v.transpose();

      // 6. Ch+1=Ch-vh*vh', Mh+1=Mh-ph*ph'
      C = C.sub(v.mul(v_trans));
      M = M.sub(p.mul(p_trans));

      // 7. Ah+1=ChAh (actually Ch+1)
      A = C.mul(A);
    }

    // finish
    if (m_NumCoefficients > 0)
      slim(W);
    m_W = W;
    m_B = W.mul(Q.transpose());

    return null;
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the transformed data
   */
  @Override
  protected Matrix doTransform(Matrix predictors) throws Exception {
    return predictors.mul(m_W);
  }

  /**
   * Returns whether the algorithm can make predictions.
   *
   * @return		true if can make predictions
   */
  public boolean canPredict() {
    return true;
  }

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the predictions
   */
  @Override
  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {
    return predictors.mul(m_B);
  }
}