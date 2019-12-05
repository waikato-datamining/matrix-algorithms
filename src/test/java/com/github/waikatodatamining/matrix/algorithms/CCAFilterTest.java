package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Tests the CCAFilter.
 *
 * @author Steven Lang
 */
public class CCAFilterTest extends MatrixAlgorithmTest<CCAFilter> {

  @TestRegression
  public void lambdaX10() {
    m_subject.setLambdaX(10);
  }

  @TestRegression
  public void lambdaY10() {
    m_subject.setLambdaY(10);
  }

  @Override
  protected void setupRegressions(CCAFilter subject, Matrix[] inputData) {
    super.setupRegressions(subject, inputData);
    addRegression(Tags.PROJECTION + "-X", subject.getProjectionMatrixX());
    addRegression(Tags.PROJECTION + "-Y", subject.getProjectionMatrixY());
  }

  @Override
  protected CCAFilter instantiateSubject() {
    return new CCAFilter();
  }
}
