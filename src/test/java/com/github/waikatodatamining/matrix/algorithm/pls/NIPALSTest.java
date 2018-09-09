package com.github.waikatodatamining.matrix.algorithm.pls;

import com.github.waikatodatamining.matrix.algorithm.pls.NIPALS.DeflationMode;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the NIPALS algorithm.
 *
 * @author Steven Lang
 */
public class NIPALSTest extends AbstractPLSTest<NIPALS> {

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
  protected NIPALS instantiateSubject() {
    return new NIPALS();
  }
}
