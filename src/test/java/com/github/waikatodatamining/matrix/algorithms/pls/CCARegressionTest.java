package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.algorithms.pls.NIPALS.DeflationMode;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the CCARegression algorithm.
 *
 * @author Steven Lang
 */
public class CCARegressionTest extends AbstractPLSTest<CCARegression> {

  @TestRegression
  public void deflationModeCanonical() {
    m_subject.setDeflationMode(DeflationMode.CANONICAL);
  }

  @TestRegression
  public void deflationModeRegression() {
    m_subject.setDeflationMode(DeflationMode.REGRESSION);
  }

  @TestRegression
  public void normYWeightsTrue() {
    m_subject.setNormYWeights(true);
  }

  @Override
  protected CCARegression instantiateSubject() {
    return new CCARegression();
  }
}
