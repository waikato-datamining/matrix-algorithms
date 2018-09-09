package com.github.waikatodatamining.matrix.algorithm.glsw;

import com.github.waikatodatamining.matrix.test.misc.TestDataset;
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
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS
    };
  }

  @Override
  protected EPO instantiateSubject() {
    return new EPO();
  }
}
