package com.github.waikatodatamining.matrix.algorithms.ica.approxfun;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.Tuple;

import java.io.Serializable;

import static com.github.waikatodatamining.matrix.core.matrix.MatrixFactory.filledLike;

/**
 * Exponential Negative Entropy Approximation Function.
 *
 * @author Steven Lang
 */
public class Exponential implements NegEntropyApproximationFunction, Serializable {

  @Override
  public Tuple<Matrix, Matrix> apply(Matrix x) {
    Matrix xPow2 = x.powElementwise(2);
    Matrix exp = xPow2.div(2).mul(-1).applyElementwise(StrictMath::exp);

    Matrix gx = x.mulElementwise(exp);


    Matrix ones = filledLike(gx, 1.0);
    Matrix g_x = ones.sub(xPow2).mulElementwise(exp).mean(1);

    return new Tuple<>(gx, g_x);
  }
}
