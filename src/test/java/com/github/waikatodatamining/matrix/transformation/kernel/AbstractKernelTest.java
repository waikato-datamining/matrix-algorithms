package com.github.waikatodatamining.matrix.transformation.kernel;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;

/**
 * Abstract kernel test. Regression for matrix transformation.
 *
 * @author Steven Lang
 */
public abstract class AbstractKernelTest<T extends AbstractKernel> extends AbstractRegressionTest<T> {

  @Override
  protected void setupRegressions(T subject, Matrix[] inputData) throws Exception {
    Matrix X = inputData[0];
    Matrix matrixResult = subject.applyMatrix(X);
    addRegression("kernel-matrix-result", matrixResult);
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS
    };
  }
}
