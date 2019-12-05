package com.github.waikatodatamining.matrix.algorithms.pls;

/**
 * Testcase for the PLS1 algorithm.
 *
 * @author Steven Lang
 */
public class PLS1Test<T extends PLS1> extends AbstractPLSTest<T> {

  @Override
  protected T instantiateSubject() {
    return (T) new PLS1();
  }
}
