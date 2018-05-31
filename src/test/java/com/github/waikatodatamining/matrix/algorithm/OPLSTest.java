/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * SIMPLSTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OPLS algorithm.
 *
 * @author Steven Lang
 */
public class OPLSTest
  extends AbstractPLSTest<OPLS> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public OPLSTest(String name) {
    super(name);
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  @Override
  protected String[] getRegressionInputFiles() {
    return new String[]{
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
    };
  }

  /**
   * Returns the filenames (without path) of the response data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  @Override
  protected String[] getRegressionResponseFiles() {
    return new String[]{
      "bolts_response.csv",
      "bolts_response.csv",
      "bolts_response.csv",
      "bolts_response.csv",
      "bolts_response.csv",
      "bolts_response.csv",
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected OPLS[] getRegressionSetups() {
    OPLS[]	result;

    result    = new OPLS[6];
    result[0] = new OPLS();
    result[0].setNumComponents(3);

    result[1] = new OPLS();
    result[1].setNumComponents(3);
    result[1].setPreprocessingType(PreprocessingType.CENTER);

    result[2] = new OPLS();
    result[2].setNumComponents(3);
    result[2].setPreprocessingType(PreprocessingType.STANDARDIZE);

    result[3] = new OPLS();
    result[3].setNumComponents(3);
    result[3].setBasePLS(new PLS1());
    result[3].setPreprocessingType(PreprocessingType.STANDARDIZE);

    result[4] = new OPLS();
    result[4].setNumComponents(3);
    result[4].setBasePLS(new SIMPLS());
    result[4].setPreprocessingType(PreprocessingType.STANDARDIZE);

    result[5] = new OPLS();
    result[5].setNumComponents(3);
    result[5].setBasePLS(new KernelPLS());
    result[5].setPreprocessingType(PreprocessingType.STANDARDIZE);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(OPLSTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    runTest(suite());
  }
}
