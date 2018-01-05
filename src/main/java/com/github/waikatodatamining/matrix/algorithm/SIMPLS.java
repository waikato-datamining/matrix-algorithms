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

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;

/**
 * SIMPLS algorithm.
 * <br>
 * See here:
 * <a href="http://www.statsoft.com/Textbook/Partial-Least-Squares#SIMPLS">SIMPLS (StatSoft)</a>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SIMPLS
  extends AbstractSingleReponsePLS {

  private static final long serialVersionUID = 4899661745515419256L;

  /** the W matrix */
  protected Matrix m_W;

  /** the B matrix (used for prediction) */
  protected Matrix m_B;

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
    A = X_trans.times(response);
    M = X_trans.times(predictors);
    C = Matrix.identity(predictors.getColumnDimension(), predictors.getColumnDimension());
    W = new Matrix(predictors.getColumnDimension(), getNumComponents());
    P = new Matrix(predictors.getColumnDimension(), getNumComponents());
    Q = new Matrix(1, getNumComponents());

    for (h = 0; h < getNumComponents(); h++) {
      // 1. qh as dominant EigenVector of Ah'*Ah
      A_trans = A.transpose();
      q = MatrixHelper.getDominantEigenVector(A_trans.times(A));

      // 2. wh=Ah*qh, ch=wh'*Mh*wh, wh=wh/sqrt(ch), store wh in W as column
      w = A.times(q);
      c = w.transpose().times(M).times(w);
      w = w.times(1.0 / StrictMath.sqrt(c.get(0, 0)));
      MatrixHelper.setColumnVector(w, W, h);

      // 3. ph=Mh*wh, store ph in P as column
      p = M.times(w);
      p_trans = p.transpose();
      MatrixHelper.setColumnVector(p, P, h);

      // 4. qh=Ah'*wh, store qh in Q as column
      q = A_trans.times(w);
      MatrixHelper.setColumnVector(q, Q, h);

      // 5. vh=Ch*ph, vh=vh/||vh||
      v = C.times(p);
      MatrixHelper.normalizeVector(v);
      v_trans = v.transpose();

      // 6. Ch+1=Ch-vh*vh', Mh+1=Mh-ph*ph'
      C = C.minus(v.times(v_trans));
      M = M.minus(p.times(p_trans));

      // 7. Ah+1=ChAh (actually Ch+1)
      A = C.times(A);
    }

    // finish
    m_W = W;
    m_B = W.times(Q.transpose());

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
    return predictors.times(m_W);
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
    return predictors.times(m_B);
  }
}