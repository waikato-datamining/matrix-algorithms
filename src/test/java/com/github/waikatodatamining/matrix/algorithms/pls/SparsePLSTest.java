package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the SparsePLS algorithm.
 *
 * @author Steven Lang
 */
public class SparsePLSTest extends AbstractPLSTest<SparsePLS> {

  @TestRegression
  public void lambda0() {
    m_subject.setLambda(0);
  }

  @TestRegression
  public void lambda05() {
    m_subject.setLambda(0.001);
  }


  @Override
  protected SparsePLS instantiateSubject() {
    SparsePLS spls = new SparsePLS();
    spls.setNumComponents(2);
    return spls;
  }
}
