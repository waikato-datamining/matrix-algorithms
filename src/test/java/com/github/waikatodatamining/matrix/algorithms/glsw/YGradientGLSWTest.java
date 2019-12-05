package com.github.waikatodatamining.matrix.algorithms.glsw;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Testcase for the YGradientGLSW algorithm.
 *
 * @author Steven Lang
 */
public class YGradientGLSWTest<T extends YGradientGLSW> extends GLSWTest<T> {

  @Override
  protected Matrix[] glswInputData(Matrix[] inputData) {
    return inputData;
  }

  @Override
  protected T instantiateSubject() {
    return (T) new YGradientGLSW();
  }

}
