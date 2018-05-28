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
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;

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
  protected void reset() {
    super.reset();

    m_r_hat = null;
    m_P     = null;
    m_W     = null;
    m_b_hat = null;
  }

  /**
   * Returns the all the available matrices.
   *
   * @return		the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "r_hat",
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
   * @throws Exception if analysis fails
   * @return null if successful, otherwise error message
   */
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix 	X_trans;
    Matrix 	W, w;
    Matrix 	T, t, t_trans;
    Matrix 	P, p, p_trans;
    double 	b;
    Matrix 	b_hat;
    int 	j;
    Matrix 	tmp;

    X_trans = predictors.transpose();

    // init
    W = new Matrix(predictors.getColumnDimension(), getNumComponents());
    P = new Matrix(predictors.getColumnDimension(), getNumComponents());
    T = new Matrix(predictors.getRowDimension(), getNumComponents());
    b_hat = new Matrix(getNumComponents(), 1);

    for (j = 0; j < getNumComponents(); j++) {
      // 1. step: wj
      w = X_trans.times(response);
      MatrixHelper.normalizeVector(w);
      MatrixHelper.setColumnVector(w, W, j);

      // 2. step: tj
      t = predictors.times(w);
      t_trans = t.transpose();
      MatrixHelper.setColumnVector(t, T, j);

      // 3. step: ^bj
      b = t_trans.times(response).get(0, 0) / t_trans.times(t).get(0, 0);
      b_hat.set(j, 0, b);

      // 4. step: pj
      p = X_trans.times(t).times(1 / t_trans.times(t).get(0, 0));
      p_trans = p.transpose();
      MatrixHelper.setColumnVector(p, P, j);

      // 5. step: Xj+1
      predictors = predictors.minus(t.times(p_trans));
      response = response.minus(t.times(b));
    }

    // W*(P^T*W)^-1
    tmp = W.times(((P.transpose()).times(W)).inverse());

    // factor = W*(P^T*W)^-1 * b_hat
    m_r_hat = tmp.times(b_hat);

    // save matrices
    m_P = P;
    m_W = W;
    m_b_hat = b_hat;

    return null;
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doTransform(Matrix predictors) throws Exception {
    Matrix 	result;
    Matrix 	T, t;
    Matrix 	x, X;
    int 	i, j;

    result = new Matrix(predictors.getRowDimension(), getNumComponents());

    for (i = 0; i < predictors.getRowDimension(); i++) {
      // work on each row
      x = MatrixHelper.rowAsVector(predictors, i);
      X = new Matrix(1, getNumComponents());
      T = new Matrix(1, getNumComponents());

      for (j = 0; j < getNumComponents(); j++) {
	MatrixHelper.setColumnVector(x, X, j);
	// 1. step: tj = xj * wj
	t = x.times(MatrixHelper.getVector(m_W, j));
	MatrixHelper.setColumnVector(t, T, j);
	// 2. step: xj+1 = xj - tj*pj^T (tj is 1x1 matrix!)
	x = x.minus(MatrixHelper.getVector(m_P, j).transpose().times(t.get(0, 0)));
      }

      MatrixHelper.setRowVector(T, result, i);
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
   * @throws Exception if analysis fails
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {
    Matrix 	result;
    Matrix 	T, t;
    Matrix 	x, X;
    int 	i, j;

    result = new Matrix(predictors.getRowDimension(), 1);

    for (i = 0; i < predictors.getRowDimension(); i++) {
      // work on each row
      x = MatrixHelper.rowAsVector(predictors, i);
      X = new Matrix(1, getNumComponents());
      T = new Matrix(1, getNumComponents());

      for (j = 0; j < getNumComponents(); j++) {
	MatrixHelper.setColumnVector(x, X, j);
	// 1. step: tj = xj * wj
	t = x.times(MatrixHelper.getVector(m_W, j));
	MatrixHelper.setColumnVector(t, T, j);
	// 2. step: xj+1 = xj - tj*pj^T (tj is 1x1 matrix!)
	x = x.minus(MatrixHelper.getVector(m_P, j).transpose().times(t.get(0, 0)));
      }

      result.set(i, 0, T.times(m_b_hat).get(0, 0));
    }

    return result;
  }
}