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
 * PLS1.java
 * Copyright (C) 2018-2024 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.core.StoppedException;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;

/**
 * PLS1 algorithm.
 * <br>
 * See here:
 * <a href="https://web.archive.org/web/20081001154431/http://statmaster.sdu.dk:80/courses/ST02/module07/module.pdf">Statmaster Module 7</a>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PLS1
  extends AbstractSingleResponsePLS {

  private static final long serialVersionUID = 4899661745515419256L;

  /** the regression vector "r-hat" */
  protected Matrix m_r_hat;

  /** the P matrix */
  protected Matrix m_P;

  /** the W matrix */
  protected Matrix m_W;

  /** the b-hat vector */
  protected Matrix m_b_hat;

  /**
   * Resets the member variables.
   */
  @Override
  protected void doReset() {
    super.doReset();

    m_r_hat = null;
    m_P     = null;
    m_W     = null;
    m_b_hat = null;
  }

  /**
   * Returns the names of all the available matrices.
   *
   * @return		the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "RegVector",
      "P",
      "W",
      "b_hat"
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
      case "RegVector":
	return m_r_hat;
      case "P":
	return m_P;
      case "W":
	return m_W;
      case "b_hat":
	return m_b_hat;
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
    return getMatrix("P");
  }

  /**
   * Initializes using the provided data.
   *
   * @param predictors the input data
   * @param response   the dependent variable(s)
   */
  protected void doPLSConfigure(Matrix predictors, Matrix response) {
    Matrix 	Xk, y;
    Matrix 	W, wk;
    Matrix 	T, tk;
    Matrix 	P, pk;
    double 	bk;
    Matrix 	b_hat;
    int 	k;
    Matrix 	tmp;

    Xk = predictors;
    y = response;

    // init
    W = MatrixFactory.zeros(predictors.numColumns(), getNumComponents());
    P = MatrixFactory.zeros(predictors.numColumns(), getNumComponents());
    T = MatrixFactory.zeros(predictors.numRows(), getNumComponents());
    b_hat = MatrixFactory.zeros(getNumComponents(), 1);

    for (k = 0; k < getNumComponents(); k++) {
      if (m_Stopped)
	throw new StoppedException();

      // 1. step: wj
      wk = calculateWeights(Xk, y);
      W.setColumn(k, wk);

      // 2. step: tj
      tk = Xk.mul(wk);
      T.setColumn(k, tk);

      // 3. step: ^bj
      double tdott = tk.vectorDot(tk);
      bk = tk.vectorDot(y) / tdott;
      b_hat.set(k, 0, bk);

      // 4. step: pj
      pk = Xk.transpose().mul(tk).div(tdott);
      P.setColumn(k, pk);

      // 5. step: Xk+1 (deflating y is not necessary)
      Xk = Xk.sub(tk.mul(pk.transpose()));
    }

    // W*(P^T*W)^-1
    tmp = W.mul(((P.transpose()).mul(W)).inverse());

    // factor = W*(P^T*W)^-1 * b_hat
    m_r_hat = tmp.mul(b_hat);

    // save matrices
    m_P = P;
    m_W = W;
    m_b_hat = b_hat;
  }

  /**
   * Calculate the weight w_k in the PLS iterations.
   *
   * @param xk X matrix at step k
   * @param y y matrix
   * @return Weights at step k
   */
  protected Matrix calculateWeights(Matrix xk, Matrix y) {
    return xk.transpose().mul(y).normalized();
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doPLSTransform(Matrix predictors) {
    Matrix 	result;
    Matrix 	T, t;
    Matrix 	x, X;
    int 	i, j;

    result = MatrixFactory.zeros(predictors.numRows(), getNumComponents());

    for (i = 0; i < predictors.numRows(); i++) {
      // work on each row
      x = MatrixHelper.rowAsVector(predictors, i);
      X = MatrixFactory.zeros(1, getNumComponents());
      T = MatrixFactory.zeros(1, getNumComponents());

      for (j = 0; j < getNumComponents(); j++) {
	if (m_Stopped)
	  throw new StoppedException();

	X.setColumn(j, x);
	// 1. step: tj = xj * wj
	t = x.mul(m_W.getColumn(j));
	T.setColumn(j, t);
	// 2. step: xj+1 = xj - tj*pj^T (tj is 1x1 matrix!)
	x = x.sub(m_P.getColumn(j).transpose().mul(t.asDouble()));
      }

      result.setRow(i, T);
    }

    return result;
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
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doPLSPredict(Matrix predictors) {
    Matrix 	result;
    Matrix 	T, t;
    Matrix 	x, X;
    int 	i, j;

    result = MatrixFactory.zeros(predictors.numRows(), 1);

    for (i = 0; i < predictors.numRows(); i++) {
      // work on each row
      x = MatrixHelper.rowAsVector(predictors, i);
      X = MatrixFactory.zeros(1, getNumComponents());
      T = MatrixFactory.zeros(1, getNumComponents());

      for (j = 0; j < getNumComponents(); j++) {
	if (m_Stopped)
	  throw new StoppedException();

	X.setColumn(j, x);
	// 1. step: tj = xj * wj
	t = x.mul(m_W.getColumn(j));
	T.setColumn(j, t);
	// 2. step: xj+1 = xj - tj*pj^T (tj is 1x1 matrix!)
	x = x.sub(m_P.getColumn(j).transpose().mul(t.asDouble()));
      }

      result.set(i, 0, T.mul(m_b_hat).asDouble());
    }

    return result;
  }
}
