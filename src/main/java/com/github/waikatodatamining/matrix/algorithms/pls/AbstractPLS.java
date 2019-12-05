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
 * AbstractPLS.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.algorithms.Center;
import com.github.waikatodatamining.matrix.algorithms.Standardize;
import com.github.waikatodatamining.matrix.core.algorithm.PredictingSupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;

/**
 * Ancestor for partial least squares variants.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractPLS
  extends PredictingSupervisedMatrixAlgorithm {

  private static final long serialVersionUID = -1160378471265135477L;

  /** the preprocessing type to perform. */
  protected PreprocessingType m_PreprocessingType = PreprocessingType.NONE;

  /** the maximum number of components to generate */
  protected int m_NumComponents = 5;

  /** the transformation for the predictors. */
  private UnsupervisedMatrixAlgorithm m_TransPredictors;

  /** the transformation for the response. */
  private UnsupervisedMatrixAlgorithm m_TransResponse;

  /**
   * Resets the member variables.
   */
  @Override
  protected void doReset() {
    m_TransPredictors = null;
    m_TransResponse   = null;
  }

  @Override
  protected void doConfigure(Matrix X, Matrix y) {
    switch (m_PreprocessingType) {
      case CENTER:
        m_TransPredictors = new Center();
        m_TransResponse   = new Center();
        break;
      case STANDARDIZE:
        m_TransPredictors = new Standardize();
        m_TransResponse   = new Standardize();
        break;
      case NONE:
        m_TransPredictors = null;
        m_TransResponse   = null;
        break;
      default:
        throw new IllegalStateException("Unhandled preprocessing type; " + m_PreprocessingType);
    }

    if (m_TransPredictors != null)
      X = m_TransPredictors.configureAndTransform(X);
    if (m_TransResponse != null)
      y = m_TransResponse.configureAndTransform(y);

    doPLSConfigure(X, y);
  }

  /**
   * PLS-specific configuration implementation. Override to configure
   * the PLS algorithm on the given matrices, after feature/target
   * normalisation has been performed.
   *
   * @param X   The normalised feature configuration matrix.
   * @param y   The normalised target configuration matrix.
   */
  protected abstract void doPLSConfigure(Matrix X, Matrix y);

  @Override
  protected Matrix doTransform(Matrix X) {
    if (m_TransPredictors != null)
      X = m_TransPredictors.transform(X);

    return doPLSTransform(X);
  }

  /**
   * Internal implementation of PLS transformation. Override
   * to implement the PLS-specific transformation code, after
   * normalisation has been performed.
   *
   * @param X   The normalised matrix to apply the algorithm to.
   * @return    The normalised matrix resulting from the transformation.
   */
  protected abstract Matrix doPLSTransform(Matrix X);

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doPredict(Matrix predictors) {
    Matrix	result;

    if (m_TransPredictors != null)
      predictors = m_TransPredictors.transform(predictors);

    result = doPLSPredict(predictors);

    if (m_TransResponse != null)
      result = m_TransResponse.inverseTransform(result);

    return result;
  }

  /**
   * PLS-specific prediction implementation. Override to predict
   * normalised target values for the given normalised feature matrix.
   *
   * @param X   The normalised feature matrix to predict against.
   * @return    The normalised predictions.
   */
  protected abstract Matrix doPLSPredict(Matrix X);

  /**
   * Sets the type of preprocessing to perform.
   *
   * @param value 	the type
   */
  public void setPreprocessingType(PreprocessingType value) {
    m_PreprocessingType = value;
    reset();
  }

  /**
   * Returns the type of preprocessing to perform.
   *
   * @return 		the type
   */
  public PreprocessingType getPreprocessingType() {
    return m_PreprocessingType;
  }

  /**
   * sets the maximum number of attributes to use.
   *
   * @param value 	the maximum number of attributes
   */
  public void setNumComponents(int value) {
    m_NumComponents = value;
    reset();
  }

  /**
   * returns the maximum number of attributes to use.
   *
   * @return 		the current maximum number of attributes
   */
  public int getNumComponents() {
    return m_NumComponents;
  }


  /**
   * Returns the all the available matrices.
   *
   * @return		the names of the matrices
   */
  public abstract String[] getMatrixNames();

  /**
   * Returns the matrix with the specified name.
   *
   * @param name	the name of the matrix
   * @return		the matrix, null if not available
   */
  public abstract Matrix getMatrix(String name);

  /**
   * Whether the algorithm supports return of loadings.
   *
   * @return		true if supported
   * @see		#getLoadings()
   */
  public abstract boolean hasLoadings();

  /**
   * Returns the loadings.
   *
   * @return		the loadings, null if not available
   */
  public abstract Matrix getLoadings();

  /**
   * Returns whether the algorithm can make predictions.
   *
   * @return		true if can make predictions
   */
  public abstract boolean canPredict();

  @Override
  public boolean isNonInvertible() {
    return true;
  }

  /**
   * For outputting some information about the algorithm.
   *
   * @return		the information
   */
  public String toString() {
    StringBuilder	result;

    result = new StringBuilder();
    result.append(getClass().getName()).append("\n");
    result.append(getClass().getName().replaceAll(".", "=")).append("\n\n");
    result.append("Debug        : " + getDebug()).append("\n");
    result.append("# components : " + getNumComponents()).append("\n");
    result.append("Preprocessing: " + getPreprocessingType()).append("\n");

    return result.toString();
  }
}
