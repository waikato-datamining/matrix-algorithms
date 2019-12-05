package com.github.waikatodatamining.matrix.algorithms.pls.kernel;

/**
 * Tests the RBF Kernel.
 *
 * @author Steven Lang
 */
public class RBFKernelTest extends AbstractKernelTest<RBFKernel> {

  @Override
  protected RBFKernel instantiateSubject() {
    return new RBFKernel();
  }
}
