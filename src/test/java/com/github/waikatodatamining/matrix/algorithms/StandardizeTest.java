package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the Standardize Transformation.
 *
 * @author Steven Lang
 */
public class StandardizeTest extends MatrixAlgorithmTest<Standardize> {

  @Test
  public void checkZeroVariance() {
    configure(m_subject, m_inputData);

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
