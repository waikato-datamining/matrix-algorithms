package com.github.waikatodatamining.matrix.algorithms.pls.kernel;

/**
 * Tests the Linear Kernel.
 *
 * @author Steven Lang
 */
public class LinearKernelTest extends AbstractKernelTest<LinearKernel> {

  @Override
  protected LinearKernel instantiateSubject() {
    return new LinearKernel();
  }
}
