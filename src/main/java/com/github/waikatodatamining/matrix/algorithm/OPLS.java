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

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;


/**
 * OPLS algorithm.
 * <br>
 * See here:
 * <a href="https://onlinelibrary.wiley.com/doi/pdf/10.1002/cem.695">Orthogonal Projections to latent structures (O-PLS)</a>
 *
 * @author Steven Lang
 */
public class OPLS
  extends AbstractSingleResponsePLS {


  private static final long serialVersionUID = -6097279189841762321L;

  /** the P matrix */
  protected Matrix m_Porth;

  /** the T matrix */
  protected Matrix m_Torth;

  /** the W matrix */
  protected Matrix m_Worth;

  /** Data with orthogonal signal components removed */
  protected Matrix m_Xosc;

  /** Base PLS that is trained on the cleaned data */
  protected AbstractPLS m_BasePLS;

  /** Get the base PLS model that is fitted on the OSC cleaned data */
  public AbstractPLS getBasePLS() {
    return m_BasePLS;
  }

  /** Set the base PLS model that is fitted on the OSC cleaned data */
  public void setBasePLS(AbstractPLS basePLS) {
    m_BasePLS = basePLS;
  }

  /**
   * Resets the member variables.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Porth = null;
    m_Worth = null;
    m_Torth = null;
  }

  @Override
  protected void initialize() {
    super.initialize();
    setBasePLS(new PLS1());
  }

  /**
   * Returns the all the available matrices.
   *
   * @return the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "P_orth",
      "W_orth",
      "T_orth"
    };
  }

  /**
   * Returns the matrix with the specified name.
   *
   * @param name the name of the matrix
   * @return the matrix, null if not available
   */
  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      case "P_orth":
	return m_Porth;
      case "W_orth":
	return m_Worth;
      case "T_orth":
	return m_Torth;
      default:
	return null;
    }
  }

  /**
   * Whether the algorithm supports return of loadings.
   *
   * @return true if supported
   * @see #getLoadings()
   */
  public boolean hasLoadings() {
    return true;
  }

  /**
   * Returns the loadings, if available.
   *
   * @return the loadings, null if not available
   */
  public Matrix getLoadings() {
    return getMatrix("P_orth");
  }

  /**
   * Initializes using the provided data.
   *
   * @param predictors the input data
   * @param response   the dependent variable(s)
   * @return null if successful, otherwise error message
   */
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix X, Xtrans, y;
    Matrix w, wOrth;
    Matrix t, tOrth;
    Matrix p, pOrth;

    X = predictors.copy();
    Xtrans = X.transpose();
    y = response;

    // init
    m_Worth = new Matrix(predictors.numColumns(), getNumComponents());
    m_Porth = new Matrix(predictors.numColumns(), getNumComponents());
    m_Torth = new Matrix(predictors.numRows(), getNumComponents());

    w = Xtrans.mul(y).mul(invL2Squared(y));
    MatrixHelper.normalizeVector(w);

    for (int currentComponent = 0; currentComponent < getNumComponents(); currentComponent++) {

      // Calculate scores vector
      t = X.mul(w).mul(invL2Squared(w));

      // Calculate loadings of X
      p = Xtrans.mul(t).mul(invL2Squared(t));

      // Orthogonalize weight
      wOrth = p.sub(w.mul(w.transpose().mul(p).mul(invL2Squared(w)).asDouble()));
      MatrixHelper.normalizeVector(wOrth);
      tOrth = X.mul(wOrth).mul(invL2Squared(wOrth));
      pOrth = Xtrans.mul(tOrth).mul(invL2Squared(tOrth));

      // Remove orthogonal components from X
      X = X.sub(tOrth.mul(pOrth.transpose()));
      Xtrans = X.transpose();

      // Store results
      m_Worth.setColumn(currentComponent, wOrth);
      m_Torth.setColumn(currentComponent, tOrth);
      m_Porth.setColumn(currentComponent, pOrth);
    }

    m_Xosc = X.copy();
    m_BasePLS.initialize(this.doTransform(predictors), response);

    return null;
  }

  /**
   * Get the inverse of the squared l2 norm.
   * @param v Input vector
   * @return 1.0 / norm2(v)^2
   */
  protected double invL2Squared(Matrix v) {
    double l2 = v.norm2();
    return 1.0 / (l2 * l2);
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   * @throws Exception if analysis fails
   */
  @Override
  protected Matrix doTransform(Matrix predictors) {
    // Remove signal from X_test that is orthogonal to y_train
    // X_clean = X_test - X_test*W_orth*P_orth^T
    Matrix T = predictors.mul(m_Worth);
    Matrix Xorth = T.mul(m_Porth.transpose());
    return predictors.sub(Xorth);
  }

  /**
   * Returns whether the algorithm can make predictions.
   *
   * @return true if can make predictions
   */
  public boolean canPredict() {
    return true;
  }

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   * @throws Exception if analysis fails
   */
  @Override
  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {
    Matrix Xtransformed = transform(predictors);
    return m_BasePLS.predict(Xtransformed);
  }
}