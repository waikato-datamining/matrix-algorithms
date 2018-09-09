package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the Standardize Transformation.
 *
 * @author Steven Lang
 */
public class StandardizeTest extends AbstractTransformationTest<Standardize> {

  @Test
  public void checkZeroVariance() {
    Matrix X = m_inputData[0];

    Matrix transform = m_subject.transform(X);


    double actualMean = transform.mean();
    double expectedMean = 0.0;
    double expectedStd = 1.0;

    Assertions.assertEquals(expectedMean, actualMean, 1e-7);

    for (int i = 0; i < transform.numColumns(); i++) {
      double actualStd = MatrixHelper.stdev(transform, i);
      Assertions.assertEquals(expectedStd, actualStd, 1e-7);
    }
  }

  @Override
  protected Standardize instantiateSubject() {
    return new Standardize();
  }
}
