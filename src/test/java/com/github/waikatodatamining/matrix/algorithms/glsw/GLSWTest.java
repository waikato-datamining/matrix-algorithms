package com.github.waikatodatamining.matrix.algorithms.glsw;

import com.github.waikatodatamining.matrix.algorithms.MatrixAlgorithmTest;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the GLSW algorithm.
 *
 * @author Steven Lang
 */
public class GLSWTest<T extends GLSW> extends MatrixAlgorithmTest<T> {

  protected void addGlswRegressions(GLSW subject, Matrix x) {
    // Add regressions
    addRegression(Tags.TRANSFORM, subject.transform(x));
    addRegression(Tags.PROJECTION, subject.getProjectionMatrix());
  }

  @TestRegression
  public void alpha1() {
    m_subject.setAlpha(1);
  }

  @TestRegression
  public void alpha100() {
    m_subject.setAlpha(100);
  }

  @Override
  protected void setupRegressions(T subject, Matrix[] inputData) {
    super.setupRegressions(subject, inputData);

    addGlswRegressions(subject, inputData[0]);
  }

  @Override
  protected Matrix[] getInputData() {
    return glswInputData(super.getInputData());
  }

  /**
   * Modifies the input data to match the requirements
   * of GLSW algorithms.
   *
   * @param inputData   The input data.
   * @return            The GLSW-modified input data.
   */
  protected Matrix[] glswInputData(Matrix[] inputData) {
    Matrix[] glswInputData = new Matrix[2];

    glswInputData[0] = inputData[0];
    glswInputData[1] = inputData[0].add(MatrixFactory.randnLike(inputData[0], 0.0, 0.1, 0));

    return glswInputData;
  }

  @Override
  protected T instantiateSubject() {
    return (T) new GLSW();
  }
}
