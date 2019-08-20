package com.github.waikatodatamining.matrix.core.api;

import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Filter API that exposes a method {@link Filter#transform(Matrix)} which takes
 * a matrix and returns a matrix based on this filter's transformation rules.
 *
 * @author Steven Lang
 */
public interface Filter {

  /**
   * Transform a given matrix into another matrix based on the filter's
   * implementation.
   *
   * @param predictors Input matrix
   * @return Transformed matrix
   * @throws Exception Transformation was not successful.
   */
  Matrix transform(Matrix predictors) throws Exception;
}
