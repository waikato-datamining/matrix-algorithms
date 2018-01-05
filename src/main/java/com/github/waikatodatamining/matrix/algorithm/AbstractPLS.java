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

import Jama.Matrix;

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

  /** the loadings. */
  protected Matrix m_Loadings;

  /** the scores. */
  protected Matrix m_Scores;

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Initialized = false;
    m_Loadings    = null;
    m_Scores      = null;
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
   * Generates the loadings. Called when initialization was successful.
   *
   * @return		the loadings
   */
  protected abstract Matrix generateLoadings();

  /**
   * Returns the loadings.
   *
   * @return		the loadings, null if not available
   */
  public Matrix getLoadings() {
    return m_Loadings;
  }

  /**
   * Returns the scores.
   *
   * @return		the scores, null if not available
   */
  public Matrix getScores() {
    return m_Scores;
  }

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
      if (m_Initialized) {
	m_Scores   = predict(predictors)[1];
	m_Loadings = generateLoadings();
      }
    }

    return result;
  }

  /**
   * Performs predictions on the data.
   *
   * @param predictors	the input data
   * @return		the transformed data and the predictions
   * @throws Exception	if analysis fails
   */
  protected abstract Matrix[] doPredict(Matrix predictors) throws Exception;

  /**
   * Performs predictions on the data.
   *
   * @param predictors	the input data
   * @return		the transformed data and the predictions
   * @throws Exception	if analysis fails
   */
  public Matrix[] predict(Matrix predictors) throws Exception {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doPredict(predictors);
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
