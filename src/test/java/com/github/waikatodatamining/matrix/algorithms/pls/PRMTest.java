package com.github.waikatodatamining.matrix.algorithms.pls;


/**
 * Testcase for the PRM algorithm.
 *
 * @author Steven Lang
 */
public class PRMTest extends AbstractPLSTest<PRM> {

  @Override
  protected PRM instantiateSubject() {
    return new PRM();
  }
}
