package com.github.waikatodatamining.matrix.algorithm.glsw;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;

/**
 * Testcase for the YGradientGLSW algorithm.
 *
 * @author Steven Lang
 */
public class YGradientGLSWTest<T extends YGradientGLSW> extends GLSWTest<T> {

  @Override
  protected void setupRegressions(YGradientGLSW subject, Matrix[] inputData) {
    // Get inputs: Simulate second instrument as x1 with noise
    Matrix X = inputData[0];
    Matrix y = inputData[1];

    // Init glsw
    subject.initialize(X, y);

    // Add regressions
    addGlswRegressions(subject, X);
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS,
      TestDataset.BOLTS_RESPONSE
    };
  }

  @Override
  protected T instantiateSubject() {
    return (T) new YGradientGLSW();
  }

}
