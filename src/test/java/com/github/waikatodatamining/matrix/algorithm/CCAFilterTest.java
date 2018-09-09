package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Tests the CCAFilter.
 *
 * @author Steven Lang
 */
public class CCAFilterTest extends AbstractRegressionTest<CCAFilter> {

  @TestRegression
  public void lambdaX10() {
    m_subject.setLambdaX(10);
  }

  @TestRegression
  public void lambdaY10() {
    m_subject.setLambdaY(10);
  }

  @Override
  protected void setupRegressions(CCAFilter subject, Matrix[] inputData) throws Exception {
    Matrix X = inputData[0];
    Matrix Y = inputData[1];

    subject.initialize(X, Y);

    addRegression(Tags.TRANSFORM, subject.transform(X));
    addRegression(Tags.PROJECTION + "-X", subject.getProjectionMatrixX());
    addRegression(Tags.PROJECTION + "-Y", subject.getProjectionMatrixY());
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS,
      TestDataset.BOLTS_RESPONSE
    };
  }

  @Override
  protected CCAFilter instantiateSubject() {
    return new CCAFilter();
  }
}
