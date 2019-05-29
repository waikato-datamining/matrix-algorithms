package com.github.waikatodatamining.matrix.algorithm.pls;


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
