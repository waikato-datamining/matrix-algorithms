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
 * CCAFilterTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.algorithm.pls.AbstractPLSTest.MatrixType;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the CCAFilter algorithm.
 *
 * @author Steven Lang
 */
public class CCAFilterTest
  extends AbstractAlgorithmTest<CCAFilter> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public CCAFilterTest(String name) {
    super(name);
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data   the data to work on
   * @param scheme the scheme to process the data with
   * @return the processed data
   */
  @Override
  protected Matrix process(Matrix data, CCAFilter scheme) {
    try {
      return scheme.transform(data);
    }
    catch (Exception e) {
      fail("Failed to transform data: " + stackTraceToString(e));
      return null;
    }
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return the filenames
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
   * @return the setups
   */
  @Override
  protected CCAFilter[] getRegressionSetups() {
    CCAFilter[] result;

    result = new CCAFilter[2];
    result[0] = new CCAFilter();
    result[1] = new CCAFilter();
    result[1].setKcca(1);
    result[1].setLambdaX(2);
    result[1].setLambdaY(2);

    return result;
  }

  /**
   * Returns the filenames (without path) of the response data files to use
   * in the regression test.
   *
   * @return the filenames
   */
  protected String[] getRegressionResponseFiles() {
    return new String[]{
      "bolts_response.csv",
      "bolts_response.csv",
    };
  }

  /**
   * Compares the processed data against previously saved output data.
   */
  public void testRegression() {
    Matrix dataInput;
    Matrix dataResponse;
    Matrix transformed;
    Matrix projX;
    Matrix projY;
    boolean ok;
    String regression;
    int i;
    String msg;
    String[] input;
    String[] response;
    CCAFilter[] setups;
    List<String> output;
    String outputCurr;
    TmpFile[] outputFiles;
    int[] ignored;

    if (m_NoRegressionTest)
      return;

    input = getRegressionInputFiles();
    response = getRegressionResponseFiles();
    output = new ArrayList<>();
    setups = getRegressionSetups();
    ignored = getRegressionIgnoredLineIndices();
    assertEquals("Number of input files and reponse files differ!", input.length, response.length);
    assertEquals("Number of input files and setups differ!", input.length, setups.length);

    // process data
    for (i = 0; i < input.length; i++) {
      dataInput = load(input[i]);
      assertNotNull("Could not load input data for regression test from " + input[i], dataInput);

      dataResponse = load(response[i]);
      assertNotNull("Could not load response data for regression test from " + response[i], dataResponse);

      msg = initialize(setups[i], dataInput, dataResponse);
      assertNull("Failed to initialize with data?", msg);

      transformed = process(dataInput, setups[i]);
      assertNotNull("Failed to transform data?", transformed);

      outputCurr = createOutputFilename(input[i], i);
      output.add(outputCurr);
      ok = save(transformed, outputCurr);
      assertTrue("Failed to save regression data (transformed)?", ok);


      projX = setups[i].getProjectionMatrixX();
      outputCurr = createOutputFilename(input[i], i, "-proj-X");
      output.add(outputCurr);
      ok = save(projX, outputCurr);
      assertTrue("Failed to save regression data (projection X)?", ok);

      projY = setups[i].getProjectionMatrixY();
      outputCurr = createOutputFilename(input[i], i, "-proj-Y");
      output.add(outputCurr);
      ok = save(projY, outputCurr);
      assertTrue("Failed to save regression data (projection Y)?", ok);
    }

    // test regression
    outputFiles = new TmpFile[output.size()];
    for (i = 0; i < output.size(); i++) {
      outputFiles[i] = new TmpFile(output.get(i));
    }
    regression = m_Regression.compare(outputFiles, ignored);

    // remove output, clean up scheme
    for (i = 0; i < output.size(); i++) {
      m_TestHelper.deleteFileFromTmp(output.get(i));
    }
    assertNull("Output differs:\n" + regression, regression);
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param scheme   the scheme to process the data with
   * @param input    the input data
   * @param response the response data
   * @return the processed data
   */
  protected String initialize(CCAFilter scheme, Matrix input, Matrix response) {
    try {
      return scheme.initialize(input, response);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + stackTraceToString(e));
      return null;
    }
  }


  /**
   * Creates an output filename based on the input filename.
   *
   * @param input      the input filename (no path)
   * @param no         the number of the test
   * @param matrixName the matrix name
   * @return the generated output filename (no path)
   */
  protected String createOutputFilename(String input, int no, String matrixName) {
    String result;
    int index;
    String ext;

    ext = "-out" + no + "-" + matrixName;

    index = input.lastIndexOf('.');
    if (index == -1) {
      result = input + ext;
    }
    else {
      result = input.substring(0, index);
      result += ext;
      result += input.substring(index);
    }

    return result;
  }


  /**
   * Returns the test suite.
   *
   * @return the suite
   */
  public static Test suite() {
    return new TestSuite(CCAFilterTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    runTest(suite());
  }
}
