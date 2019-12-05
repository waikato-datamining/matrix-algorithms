package com.github.waikatodatamining.matrix.algorithms.ica;

import com.github.waikatodatamining.matrix.algorithms.MatrixAlgorithmTest;
import com.github.waikatodatamining.matrix.algorithms.ica.FastICA.Algorithm;
import com.github.waikatodatamining.matrix.algorithms.ica.approxfun.Cube;
import com.github.waikatodatamining.matrix.algorithms.ica.approxfun.Exponential;
import com.github.waikatodatamining.matrix.algorithms.ica.approxfun.LogCosH;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import org.junit.Assert;

public class FastICATest extends MatrixAlgorithmTest<FastICA> {

  @TestRegression
  public void deflation() {
    m_subject.setAlgorithm(Algorithm.DEFLATION);
  }

  @TestRegression
  public void parallel() {
    m_subject.setAlgorithm(Algorithm.PARALLEL);
  }

  @TestRegression
  public void whiteFalse() {
    m_subject.setWhiten(false);
  }

  @TestRegression
  public void logcosh() {
    m_subject.setFun(new LogCosH());
  }

  @TestRegression
  public void cube() {
    m_subject.setFun(new Cube());
  }

  @TestRegression
  public void exp() {
    m_subject.setFun(new Exponential());
  }

  @Override
  protected void setupRegressions(FastICA subject, Matrix[] inputData) {
    super.setupRegressions(subject, inputData);

    Matrix X = inputData[0];

    try {
      Matrix transform = subject.transform(X);
      addRegression("transform", transform);
      addRegression("components", subject.getComponents());
      addRegression("mixing", subject.getMixing());
      addRegression("sources", subject.getSources());
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Exception during transform.");
    }
  }

  @Override
  protected FastICA instantiateSubject() {
    return new FastICA();
  }
}
