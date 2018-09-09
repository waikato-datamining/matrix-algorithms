package com.github.waikatodatamining.matrix.algorithm.pls;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.waikatodatamining.matrix.core.MatrixFactory.randnLike;

/**
 * Testcase for the DIPLS algorithm.
 *
 * @author Steven Lang
 */
public class DIPLSTest extends AbstractPLSTest<DIPLS> {

  @TestRegression
  public void lambda01() {
    m_subject.setLambda(0.01);
  }

  @Override
  protected void setupRegressions(DIPLS subject, Matrix[] inputData) throws Exception {
    Matrix xSourceDomain = inputData[0];
    Matrix ySourceDomain = inputData[1];
    Matrix xTargetDomain = xSourceDomain.add(randnLike(xSourceDomain, 1, 2, 0));
    Matrix yTargetDomain = ySourceDomain.add(randnLike(ySourceDomain, 1, 2, 1));
    Matrix xTargetDomainUnlabeled = xSourceDomain.add(randnLike(xSourceDomain, 1, 2, 100));


    // Initialize supervised
    subject.initializeSupervised(xSourceDomain, xTargetDomain, ySourceDomain, yTargetDomain);
    addDefaultPlsMatrices(subject, xTargetDomain, Tags.SUPERVISED);
    subject.reset();

    // Initialize unsupervised
    subject.initializeUnsupervised(xSourceDomain, xTargetDomain, ySourceDomain);
    addDefaultPlsMatrices(subject, xTargetDomain, Tags.UNSUPERVISED);
    subject.reset();

    // Initialize semisupervised
    subject.initializeSemiSupervised(xSourceDomain, xTargetDomain, xTargetDomainUnlabeled, ySourceDomain, yTargetDomain);
    addDefaultPlsMatrices(subject, xTargetDomain, Tags.SEMISUPERVISED);
  }

  @Override
  @Test
  public void checkTransformedNumComponents() throws Exception {
    Matrix X = m_inputData[0];
    Matrix X2 = X.add(randnLike(X, 0, 2, 0));
    Matrix Y = m_inputData[1];

    for (int i = 1; i < 5; i++) {
      m_subject.setNumComponents(i);
      m_subject.initializeUnsupervised(X, X2, Y);
      Matrix transform = m_subject.transform(X);
      Assertions.assertEquals(i, transform.numColumns());

      // Reset
      m_subject = instantiateSubject();
    }
  }

  @Override
  protected DIPLS instantiateSubject() {
    return new DIPLS();
  }
}
