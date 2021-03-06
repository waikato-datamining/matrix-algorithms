package com.github.waikatodatamining.matrix.algorithms.pls.kernel;

/**
 * Tests the Poly Kernel.
 *
 * @author Steven Lang
 */
public class PolyKernelTest extends AbstractKernelTest<PolyKernel> {

  @Override
  protected PolyKernel instantiateSubject() {
    return new PolyKernel();
  }
}
