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
 * PCATest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import Jama.Matrix;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the PCA algorithm.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PCATest
  extends AbstractAlgorithmTest<PCA> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public PCATest(String name) {
    super(name);
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data	the data to work on
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  @Override
  protected Matrix process(Matrix data, PCA scheme) {
    try {
      return scheme.transform(data);
    }
    catch (Exception e) {
      fail("Failed to transform data: " + e);
      return null;
    }
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
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected PCA[] getRegressionSetups() {
    PCA[]	result;

    result    = new PCA[2];
    result[0] = new PCA();
    result[1] = new PCA();
    result[1].setCenter(true);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(PCATest.class);
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
