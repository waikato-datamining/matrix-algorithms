package com.github.waikatodatamining.matrix.algorithm.glsw;

/**
 * External Parameter Orthogonalization (EPO) Test.
 *
 * @author Steven Lang
 */
public class YGradientEPOTest extends YGradientGLSWTest {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public YGradientEPOTest(String name) {
    super(name);
  }

  @Override
  protected YGradientGLSW[] getRegressionSetups() {
    return new YGradientEPO[]{new YGradientEPO()};
  }
}
