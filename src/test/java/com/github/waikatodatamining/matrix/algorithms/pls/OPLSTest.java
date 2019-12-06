package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import org.junit.jupiter.api.Disabled;

/**
 * Testcase for the OPLS algorithm.
 *
 * @author Steven Lang
 */
public class OPLSTest extends AbstractPLSTest<OPLS> {

  @Override
  public void checkTransformedNumComponents() throws Exception {
    // Do nothing since OPLS.transform(X) removes the signal from X_test that is
    // orthogonal to y_train and does not change its shape
  }

  @TestRegression
  public void baseNIPALS() {
    m_subject.setBasePLS(new NIPALS());
  }

  @TestRegression
  public void baseKernelPLS() {
    m_subject.setBasePLS(new KernelPLS());
  }

  @TestRegression
  public void basePLS1() {
    m_subject.setBasePLS(new PLS1());
  }

  @TestRegression
  public void baseSIMPLS() {
    m_subject.setBasePLS(new SIMPLS());
  }

  @TestRegression
  @Disabled("Causes MatrixInversionException")
  public void baseCCARegression() {
    m_subject.setBasePLS(new CCARegression());
  }

  @TestRegression
  public void basePRM() {
    m_subject.setBasePLS(new PRM());
  }

  @TestRegression
  public void baseSparsePLS() {
    m_subject.setBasePLS(new SparsePLS());
  }


  @Override
  protected OPLS instantiateSubject() {
    return new OPLS();
  }
}
