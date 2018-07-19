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
 * AbstractPLSTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.test.TmpFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradient Least Squares Weighting Test.
 *
 * @author Steven Lang
 */
public class GLSWTest
  extends AbstractAlgorithmTest<GLSW> {

  public enum MatrixType {
    TRANSFORMED,
    PROJECTION
  }

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public GLSWTest(String name) {
    super(name);
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param scheme	the scheme to process the data with
   * @param input	the input data
   * @param response	the response data
   * @return		the processed data
   */
  protected String initialize(GLSW scheme, Matrix input, Matrix response) {
    try {
      return scheme.initialize(input, response);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + stackTraceToString(e));
      return null;
    }
  }

  /**
   * Dummy.
   *
   * @param data	the data to work on
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  protected Matrix process(Matrix data, GLSW scheme) {
    if (!scheme.isInitialized())
      fail("Algorithm is not initialized!");
    try {
      return scheme.transform(data);
    }
    catch (Exception e) {
      fail("Failed to perform prediction: " + stackTraceToString(e));
      return null;
    }
  }

  @Override
  protected String[] getRegressionInputFiles() {
    return new String[]{
      "bolts.csv"
    };
  }

  @Override
  protected GLSW[] getRegressionSetups() {
    return new GLSW[]{new GLSW()};
  }

  /**
   * Saves the matrix to the specified file.
   *
   * @param data	the data to save
   * @param filename	the file to save to
   * @return		true if successful
   */
  protected boolean save(Matrix[] data, String filename) {
    try {
      MatrixHelper.write(data[0].concat(data[1], 1), m_TestHelper.getTmpLocationFromResource(filename), true, '\t', 6);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Creates an output filename based on the input filename.
   *
   * @param input	the input filename (no path)
   * @param no		the number of the test
   * @param type	the matrix type
   * @return		the generated output filename (no path)
   */
  protected String createOutputFilename(String input, int no, MatrixType type) {
    String	result;
    int		index;
    String	ext;

    ext = "-out" + no + "-" + type.toString().toLowerCase();

    index = input.lastIndexOf('.');
    if (index == -1) {
      result = input + ext;
    }
    else {
      result  = input.substring(0, index);
      result += ext;
      result += input.substring(index);
    }

    return result;
  }

  /**
   * Compares the processed data against previously saved output data.
   */
  public void testRegression() {
    Matrix 		dataInput;
    Matrix 		dataInputMockSecondInstrument;
    Matrix 		transformed;
    Matrix 		projection;
    boolean		ok;
    String		regression;
    int			i;
    String		msg;
    String[]		input;
    GLSW[]			setups;
    List<String> 	output;
    String		outputCurr;
    TmpFile[]		outputFiles;
    int[]		ignored;

    if (m_NoRegressionTest)
      return;

    input    = getRegressionInputFiles();
    output   = new ArrayList<>();
    setups   = getRegressionSetups();
    ignored  = getRegressionIgnoredLineIndices();
    assertEquals("Number of input files and setups differ!", input.length, setups.length);

    // process data
    for (i = 0; i < input.length; i++) {
      dataInput = load(input[i]);
      assertNotNull("Could not load input data for regression test from " + input[i], dataInput);

      // Create mock dataset from input (assume the same data was measured on another instrument)
      dataInputMockSecondInstrument = dataInput.add(MatrixFactory.randnLike(dataInput, 0.0, 0.1, 0));

      msg = initialize(setups[i], dataInput, dataInputMockSecondInstrument);
      assertNull("Failed to initialize with data?", msg);

      transformed = process(dataInput, setups[i]);
      assertNotNull("Failed to transform data?", transformed);

      projection = setups[i].getProjectionMatrix();
      assertNotNull("Projection matrix was null after initialization.", projection);

      outputCurr = createOutputFilename(input[i], i, MatrixType.PROJECTION);
      output.add(outputCurr);
      ok        = save(projection, outputCurr);
      assertTrue("Failed to save projection matrix?", ok);

      outputCurr = createOutputFilename(input[i], i, MatrixType.TRANSFORMED);
      output.add(outputCurr);
      ok        = save(transformed, outputCurr);
      assertTrue("Failed to save regression data (transformed)?", ok);
    }

    // test regression
    outputFiles = new TmpFile[output.size()];
    for (i = 0; i < output.size(); i++){
      outputFiles[i] = new TmpFile(output.get(i));
    }
    regression = m_Regression.compare(outputFiles, ignored);

    // remove output, clean up scheme
    for (i = 0; i < output.size(); i++) {
      m_TestHelper.deleteFileFromTmp(output.get(i));
    }
    assertNull("Output differs:\n" + regression, regression);
  }
}
