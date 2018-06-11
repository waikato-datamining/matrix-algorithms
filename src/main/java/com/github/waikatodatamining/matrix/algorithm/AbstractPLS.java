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

package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Ancestor for partial least squares variants.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractPLS
  extends AbstractAlgorithm {

  private static final long serialVersionUID = -1160378471265135477L;

  /** the preprocessing type to perform. */
  protected PreprocessingType m_PreprocessingType;

  /** the maximum number of components to generate */
  protected int m_NumComponents;

  /** whether the algorithm has been initialized. */
  protected boolean m_Initialized;

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Initialized = false;
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    setNumComponents(5);
    setPreprocessingType(PreprocessingType.NONE);
  }

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
   * Returns whether the algorithm has been trained.
   *
   * @return		true if trained
   */
  public boolean isInitialized() {
    return m_Initialized;
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
   * Hook method for checking the data before training.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   * @return		null if successful, otherwise error message
   */
  protected String check(Matrix predictors, Matrix response) {
    if (predictors == null)
      return "No predictors matrix provided!";
    if (response == null)
      return "No response matrix provided!";
    return null;
  }

  /**
   * Trains using the provided data.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  protected abstract String doInitialize(Matrix predictors, Matrix response) throws Exception;

  /**
   * Initializes using the provided data.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  public String initialize(Matrix predictors, Matrix response) throws Exception {
    String	result;

    reset();

    result = check(predictors, response);

    if (result == null) {
      result        = doInitialize(predictors, response);
      m_Initialized = (result == null);
    }

    return result;
  }

  /**
   * Returns whether the algorithm can make predictions.
   *
   * @return		true if can make predictions
   */
  public abstract boolean canPredict();

  /**
   * Performs predictions on the data.
   *
   * @param predictors	the input data
   * @return		the predictions
   * @throws Exception	if analysis fails
   */
  protected abstract Matrix doPredict(Matrix predictors) throws Exception;

  /**
   * Performs predictions on the data.
   *
   * @param predictors	the input data
   * @return		the predictions
   * @throws Exception	if analysis fails
   */
  public Matrix predict(Matrix predictors) throws Exception {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doPredict(predictors);
  }

  /**
   * Transforms the data.
   *
   * @param predictors	the input data
   * @return		the transformed data
   * @throws Exception	if analysis fails
   */
  protected abstract Matrix doTransform(Matrix predictors) throws Exception;

  /**
   * Transforms the data.
   *
   * @param predictors	the input data
   * @return		the transformed data
   * @throws Exception	if analysis fails
   */
  public Matrix transform(Matrix predictors) throws Exception {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doTransform(predictors);
  }


  /**
   * Initializes using the provided data.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  protected abstract String doPerformInitialization(Matrix predictors, Matrix response) throws Exception;

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the transformed data and the predictions
   */
  protected abstract Matrix doPerformPredictions(Matrix predictors) throws Exception;

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
