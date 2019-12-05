package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.algorithm.MatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.algorithm.SupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
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
public abstract class MatrixAlgorithmTest<T extends MatrixAlgorithm>
  extends AbstractRegressionTest<T> {

  @Test
  public void checkInvTransformEqInput() {
    configure(m_subject, m_inputData);

    // Non-invertible algorithms get an automatic pass
    if (m_subject.isNonInvertible())
      return;

    Matrix input = m_inputData[0];
    Matrix transform = m_subject.transform(input);
    Matrix inverseTransform = m_subject.inverseTransform(transform);

    // Check if input == invTransform(transform(input))
    boolean isEqual = input.sub(inverseTransform).abs().all(v -> v < 1e-7);
    Assertions.assertTrue(isEqual);
  }

  @Override
  protected void setupRegressions(T subject, Matrix[] inputData) {
    Matrix X = inputData[0];

    configure(subject, inputData);

    Matrix transform = subject.transform(X);
    addRegression(Tags.TRANSFORM, transform);

    if (!subject.isNonInvertible()) {
      Matrix inverseTransform = subject.inverseTransform(transform);
      addRegression(Tags.INVERSE_TRANSFORM, inverseTransform);
    }
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS,
      TestDataset.BOLTS_RESPONSE
    };
  }

  /**
   * Configures the subject algorithm on the input data,
   * if it is required.
   *
   * @param subject     The subject algorithm.
   * @param inputData   The input data.
   */
  protected void configure(T subject, Matrix[] inputData) {
    // Unpack the data matrices
    Matrix X = inputData[0];
    Matrix y = inputData[1];

    // Perform configuration for the type of algorithm
    if (subject instanceof SupervisedMatrixAlgorithm)
      ((SupervisedMatrixAlgorithm) subject).configure(X, y);
    else if (subject instanceof UnsupervisedMatrixAlgorithm)
      ((UnsupervisedMatrixAlgorithm) subject).configure(X);
  }
}
