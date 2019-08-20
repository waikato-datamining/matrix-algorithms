package com.github.waikatodatamining.matrix.core.api;

import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Filter API that exposes a method
 * {@link ResponseFilter#transformResponse(Matrix)}
 * which takes a matrix and returns a matrix based on this filter's
 * transformation rules.
 *
 * @author Steven Lang
 */
public interface ResponseFilter {

  /**
   * Transform a given matrix into another matrix based on the filter's
   * implementation.
   *
   * @param response Input matrix
   * @return Transformed matrix
   * @throws Exception Transformation was not successful.
   */
  Matrix transformResponse(Matrix response) throws Exception;
}
