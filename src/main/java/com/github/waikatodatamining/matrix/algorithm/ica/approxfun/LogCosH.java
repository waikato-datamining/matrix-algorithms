package com.github.waikatodatamining.matrix.algorithm.ica.approxfun;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.Tuple;

import static com.github.waikatodatamining.matrix.core.MatrixFactory.filled;
import static com.github.waikatodatamining.matrix.core.MatrixFactory.zeros;

/**
 * LogCosH Negative Entropy Approximation Function.
 *
 * @author Steven Lang
 */
public class LogCosH implements NegEntropyApproximationFunction {

  /** Alpha variable for logCosH */
  protected double m_alpha;

  /**
   * Constructor with alpha value.
   *
   * @param alpha Alpha
   */
  public LogCosH(double alpha) {
    m_alpha = alpha;
  }

  /**
   * Default constructor.
   * Sets alpha to 1.0 by default.
   */
  public LogCosH() {
    m_alpha = 1.0;
  }

  /**
   * Get alpha value.
   *
   * @return Alpha value
   */
  public double getAlpha() {
    return m_alpha;
  }

  /**
   * Set alpha value.
   *
   * @param alpha Alpha value
   */
  public void setAlpha(double alpha) {
    m_alpha = alpha;
  }

  @Override
  public Tuple<Matrix, Matrix> apply(Matrix x) {
    x = x.mul(m_alpha);
    Matrix gx = x.applyElementwise(StrictMath::tanh);
    Matrix g_x = zeros(gx.numRows(), 1);
    Matrix ones = filled(1, gx.numColumns(), 1.0);
    for (int i = 0; i < gx.numRows(); i++) {
      Matrix gxi = gx.getRow(i);
      double g_xi = ones.sub(gxi.powElementwise(2)).mul(m_alpha).mean(-1).asDouble();
      g_x.set(i, 0, g_xi);
    }
    return new Tuple<>(gx, g_x);
  }
}
