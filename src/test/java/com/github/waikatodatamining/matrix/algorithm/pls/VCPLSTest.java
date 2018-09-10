package com.github.waikatodatamining.matrix.algorithm.pls;

/**
 * Testcase for the VCPLS algorithm.
 *
 * @author Steven Lang
 */
public class VCPLSTest extends AbstractPLSTest<VCPLS> {

  @Override
  protected VCPLS instantiateSubject() {
    return new VCPLS();
  }
}
