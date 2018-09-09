package com.github.waikatodatamining.matrix.algorithm.pls;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import com.github.waikatodatamining.matrix.transformation.kernel.LinearKernel;
import com.github.waikatodatamining.matrix.transformation.kernel.PolyKernel;
import com.github.waikatodatamining.matrix.transformation.kernel.RBFKernel;

/**
 * Testcase for the KernelPLS algorithm.
 *
 * @author Steven Lang
 */
public class KernelPLSTest extends AbstractPLSTest<KernelPLS> {

  @TestRegression
  public void linearKernel() {
    m_subject.setKernel(new LinearKernel());
  }

  @TestRegression
  public void polyKernel() {
    m_subject.setKernel(new PolyKernel());
  }

  @TestRegression
  public void rbfKernel() {
    m_subject.setKernel(new RBFKernel());
  }

  @Override
  protected KernelPLS instantiateSubject() {
    return new KernelPLS();
  }
}
