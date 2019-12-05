package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Test the PCA class.
 *
 * @author Steven Lang
 */
public class PCATest extends MatrixAlgorithmTest<PCA> {

  @TestRegression
  public void center() {
    m_subject.setCenter(true);
  }

  @TestRegression
  public void maxCols3() {
    m_subject.setMaxColumns(3);
  }


  @Override
  protected void setupRegressions(PCA subject, Matrix[] inputData) {
    super.setupRegressions(subject, inputData);

    // Get input
    Matrix X = inputData[0];

    // Get matrices
    Matrix transformed = subject.transform(X);
    Matrix loadings = subject.getLoadings();
    Matrix scores = subject.getScores();

    // Add regressions
    addRegression(Tags.TRANSFORM, transformed);
    addRegression(Tags.LOADINGS, loadings);
    addRegression(Tags.SCORES, scores);
  }

  @Override
  protected PCA instantiateSubject() {
    return new PCA();
  }
}
