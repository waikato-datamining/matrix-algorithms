package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the Center Transformation.
 *
 * @author Steven Lang
 */
public class CenterTest extends AbstractTransformationTest<Center> {

  @Test
  public void meanIsZero() {
    Matrix X = m_inputData[0];
    Matrix transform = m_subject.transform(X);
    double actual = transform.mean();
    double expected = 0.0;
    Assertions.assertEquals(expected, actual, 1e-7);
  }

  @Override
  protected Center instantiateSubject() {
    return new Center();
  }
}
