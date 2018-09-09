package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test PassThrough transformation.
 *
 * @author Steven Lang
 */
public class PassThroughTest extends AbstractTransformationTest<PassThrough> {

  @Test
  public void resultUnchanged() {
    Matrix X = m_inputData[0];
    Matrix transform = m_subject.transform(X);

    boolean isEqual = X.sub(transform).abs().all(v -> v < 1e-7);
    Assertions.assertTrue(isEqual);
  }

  @Override
  protected PassThrough instantiateSubject() {
    return new PassThrough();
  }
}
