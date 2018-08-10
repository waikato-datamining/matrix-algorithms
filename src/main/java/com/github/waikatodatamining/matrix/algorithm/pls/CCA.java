package com.github.waikatodatamining.matrix.algorithm.pls;

/**
 * CCA Canonical Correlation Analysis.
 *
 * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.30.16">A Survey of Partial Least Squares (PLS) Methods, with Emphasis on the Two-Block Case (2000)</a>
 *
 * Parameters: See also {@link NIPALS}.
 * - tol: Iterative convergence tolerance
 * - maxIter: Maximum number of iterations
 * - normYWeights: Flat to normalize Y weights
 *
 * @author Steven Lang
 */
public class CCA extends NIPALS {

  private static final long serialVersionUID = -5265196640192613371L;

  @Override
  protected WeightCalculationMode getWeightCalculationMode() {
    return WeightCalculationMode.CCA; // Mode B in sklearn
  }

  @Override
  public DeflationMode getDeflationMode() {
    return DeflationMode.CANONICAL;
  }

  @Override
  public void setDeflationMode(DeflationMode deflationMode) {
    if (deflationMode != DeflationMode.CANONICAL){
      m_Logger.warning("CCA only allows CANONICAL deflation mode.");
    }
  }
}
