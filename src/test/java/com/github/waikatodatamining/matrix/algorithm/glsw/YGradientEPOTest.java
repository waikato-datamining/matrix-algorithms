package com.github.waikatodatamining.matrix.algorithm.glsw;

/**
 * Testcase for the YGradientEPO algorithm.
 *
 * @author Steven Lang
 */
public class YGradientEPOTest<T extends YGradientEPO> extends YGradientGLSWTest<T> {


  @Override
  protected T instantiateSubject() {
    return (T) new YGradientEPO();
  }

}
