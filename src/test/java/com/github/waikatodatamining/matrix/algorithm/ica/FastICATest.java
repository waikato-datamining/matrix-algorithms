package com.github.waikatodatamining.matrix.algorithm.ica;

import com.github.waikatodatamining.matrix.algorithm.ica.FastICA.Algorithm;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.Cube;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.Exponential;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.LogCosH;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import org.junit.Assert;

public class FastICATest extends AbstractRegressionTest<FastICA> {

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
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS
    };
  }

  @Override
  protected FastICA instantiateSubject() {
    return new FastICA();
  }
}
