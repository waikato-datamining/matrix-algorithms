package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Estimator API that exposes a method {@link Estimator#predict(Matrix)} which takes
 * a data matrix and returns the predictions for the data.
 *
 * @author Steven Lang
 */
public interface Estimator {

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the predictions
   */
  Matrix predict(Matrix predictors) throws Exception;
}
