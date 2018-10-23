package com.github.waikatodatamining.matrix.algorithm.ica.approxfun;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.Tuple;

import java.io.Serializable;

/**
 * Cubic Negative Entropy Approximation Function.
 *
 * @author Steven Lang
 */
public class Cube implements NegEntropyApproximationFunction, Serializable {

  @Override
  public Tuple<Matrix, Matrix> apply(Matrix x) {
    Matrix gx = x.powElementwise(3);
    Matrix g_x = x.powElementwise(2).mul(3).mean(1);
    return new Tuple<>(gx, g_x);
  }
}
