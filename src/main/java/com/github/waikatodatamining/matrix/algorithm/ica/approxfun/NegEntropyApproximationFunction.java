package com.github.waikatodatamining.matrix.algorithm.ica.approxfun;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.Tuple;

/**
 * Negative Entropy Approximation Function.
 *
 * @author Steven Lang
 */
public interface NegEntropyApproximationFunction {

  /**
   * Apply the approximation function to the given matrix.
   *
   * @param x Input matrix
   * @return Output tuple containing approximation function return value and its
   * derivative
   */
  Tuple<Matrix, Matrix> apply(Matrix x);
}
