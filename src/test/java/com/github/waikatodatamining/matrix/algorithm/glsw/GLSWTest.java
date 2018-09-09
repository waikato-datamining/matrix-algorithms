package com.github.waikatodatamining.matrix.algorithm.glsw;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Testcase for the GLSW algorithm.
 *
 * @author Steven Lang
 */
public class GLSWTest<T extends GLSW> extends AbstractRegressionTest<T> {

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
  protected void setupRegressions(GLSW subject, Matrix[] inputData) {
    // Get inputs: Simulate second instrument as x1 with noise
    Matrix xFirstInstrument = inputData[0];
    Matrix xSecondInstrument = xFirstInstrument.add(MatrixFactory.randnLike(xFirstInstrument, 0.0, 0.1, 0));

    // Init glsw
    subject.initialize(xFirstInstrument, xSecondInstrument);

    addGlswRegressions(subject, xFirstInstrument);
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS,
    };
  }

  @Override
  protected T instantiateSubject() {
    return (T) new GLSW();
  }
}
