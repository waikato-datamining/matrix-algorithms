package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the SIMPLS algorithm.
 *
 * @author Steven Lang
 */
public class SIMPLSTest extends AbstractPLSTest<SIMPLS> {

  @TestRegression
  public void numCoefficients1() {
    m_subject.setNumCoefficients(1);
  }

  @TestRegression
  public void numCoefficients2() {
    m_subject.setNumCoefficients(2);
  }

  @TestRegression
  public void numCoefficients3() {
    m_subject.setNumCoefficients(3);
  }

  @Override
  protected SIMPLS instantiateSubject() {
    return new SIMPLS();
  }
}
