package com.github.waikatodatamining.matrix.algorithm;

/**
 * External Parameter Orthogonalization (EPO) Test.
 *
 * @author Steven Lang
 */
public class EPOTest extends GLSWTest {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public EPOTest(String name) {
    super(name);
  }

  @Override
  protected GLSW[] getRegressionSetups() {
    return new EPO[]{new EPO()};
  }
}