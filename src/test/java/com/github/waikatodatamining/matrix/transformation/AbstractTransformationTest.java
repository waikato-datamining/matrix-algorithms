package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Abstract transformation test. Regression for transform and inverse-transform.
 *
 * @param <T> AbstractTransformation implementation
 * @author Steven Lang
 */
public abstract class AbstractTransformationTest<T extends AbstractTransformation> extends AbstractRegressionTest<T> {

  @Test
  public void checkInvTransformEqInput() {
    Matrix input = m_inputData[0];
    Matrix transform = m_subject.transform(input);
    Matrix inverseTransform = m_subject.inverseTransform(transform);

    // Check if input == invTransform(transform(input))
    boolean isEqual = input.sub(inverseTransform).abs().all(v -> v < 1e-7);
    Assertions.assertTrue(isEqual);
  }

  @Override
  protected void setupRegressions(T subject, Matrix[] inputData) throws Exception {
    Matrix X = inputData[0];
    subject.configure(X);

    Matrix transform = subject.transform(X);
    Matrix inverseTransform = subject.inverseTransform(transform);
    addRegression(Tags.TRANSFORM, transform);
    addRegression(Tags.INVERSE_TRANSFORM, inverseTransform);
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS
    };
  }
}
