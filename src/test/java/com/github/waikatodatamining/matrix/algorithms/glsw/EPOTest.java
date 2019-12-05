package com.github.waikatodatamining.matrix.algorithms.glsw;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the EPO algorithm.
 *
 * @author Steven Lang
 */
public class EPOTest extends GLSWTest<EPO> {

  @TestRegression
  public void n1() {
    m_subject.setN(1);
  }

  @TestRegression
  public void n3() {
    m_subject.setN(3);
  }

  @Override
  protected EPO instantiateSubject() {
    return new EPO();
  }
}
