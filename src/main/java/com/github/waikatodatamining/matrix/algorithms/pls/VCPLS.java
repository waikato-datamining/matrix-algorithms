package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;

/**
 * Variance constrained partial least squares
 * <p>
 * See also:
 * <a href="http://or.nsfc.gov.cn/bitstream/00001903-5/485833/1/1000013952154.pdf">Variance
 * constrained partial least squares</a>
 * <p>
 * Parameters:
 * - lambda: (No description given in paper)
 *
 * @author Steven Lang
 */
public class VCPLS extends PLS1 {

  private static final long serialVersionUID = 8636605558345875267L;

  /** The lambda parameter. */
  protected double m_lambda = 1.0;

  /** The constant NU. */
  protected static final double NU = 1e-7;

  /**
   * Gets lambda.
   *
   * @return the lambda
   */
  public double getLambda() {
    return m_lambda;
  }


  /**
   * Sets lambda.
   *
   * @param lambda the lambda
   */
  public void setLambda(double lambda) {
    m_lambda = lambda;
    reset();
  }

  @Override
  protected Matrix calculateWeights(Matrix xk, Matrix y) {
    // Paper notation
    Matrix e = xk;
    Matrix f = y;

    Matrix I = MatrixFactory.eye(e.numColumns());
    Matrix g1 = e.t().mul(f).mul(f.t()).mul(e).sub(I.mul(m_lambda));
    Matrix g2 = e.t().mul(e);

    Matrix term = (g2.add(I.mul(NU))).inverse().mul(g1);

    return term.getDominantEigenvector();
  }
}
