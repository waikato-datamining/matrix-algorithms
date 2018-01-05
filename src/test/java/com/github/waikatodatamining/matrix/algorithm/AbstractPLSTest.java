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

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.test.TmpFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Ancestor for PLS algorithm tests.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractPLSTest<T extends AbstractPLS>
  extends AbstractAlgorithmTest<T> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public AbstractPLSTest(String name) {
    super(name);
  }

  /**
   * Returns the filenames (without path) of the response data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected abstract String[] getRegressionResponseFiles();

  /**
   * Processes the input data and returns the processed data.
   *
   * @param scheme	the scheme to process the data with
   * @param input	the input data
   * @param response	the response data
   * @return		the processed data
   */
  protected String initialize(T scheme, Matrix input, Matrix response) {
    try {
      return scheme.initialize(input, response);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + e);
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
  protected Matrix process(Matrix data, T scheme) {
    return data;
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param scheme	the scheme to process the data with
   * @param data	the data to work on
   * @return		the transformed data and predictions
   */
  protected Matrix[] process(T scheme, Matrix data) {
    if (!scheme.isInitialized())
      fail("Algorithm is not initialized!");
    try {
      return scheme.predict(data);
    }
    catch (Exception e) {
      fail("Failed to perform prediction: " + e);
      return null;
    }
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
      MatrixHelper.write(MatrixHelper.merge(data[0], data[1]), m_TestHelper.getTmpLocationFromResource(filename), true, '\t', 6);
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
   * @param scores	whether scores or loadings
   * @return		the generated output filename (no path)
   */
  protected String createOutputFilename(String input, int no, boolean scores) {
    String	result;
    int		index;
    String	ext;

    ext = "-out" + no + (scores ? "-scores" : "-loadings");

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
    Matrix 		dataResponse;
    Matrix[]		processed;
    Matrix		loadings;
    boolean		ok;
    String		regression;
    int			i;
    String		msg;
    String[]		input;
    String[]		response;
    T[]			setups;
    List<String> 	output;
    String		outputCurr;
    TmpFile[]		outputFiles;
    int[]		ignored;

    if (m_NoRegressionTest)
      return;

    input    = getRegressionInputFiles();
    response = getRegressionResponseFiles();
    output   = new ArrayList<>();
    setups   = getRegressionSetups();
    ignored  = getRegressionIgnoredLineIndices();
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

      processed = process(setups[i], dataInput);
      assertNotNull("Failed to process data?", processed);

      outputCurr = createOutputFilename(input[i], i, true);
      output.add(outputCurr);
      ok        = save(processed, outputCurr);
      assertTrue("Failed to save regression data (scores)?", ok);

      loadings = setups[i].getLoadings();
      if (loadings != null) {
	outputCurr = createOutputFilename(input[i], i, false);
	output.add(outputCurr);
	ok = save(loadings, outputCurr);
	assertTrue("Failed to save regression data (loadings)?", ok);
      }
    }

    // test regression
    outputFiles = new TmpFile[output.size()];
    for (i = 0; i < output.size(); i++)
      outputFiles[i] = new TmpFile(output.get(i));
    regression = m_Regression.compare(outputFiles, ignored);
    assertNull("Output differs:\n" + regression, regression);

    // remove output, clean up scheme
    for (i = 0; i < output.size(); i++) {
      m_TestHelper.deleteFileFromTmp(output.get(i));
    }
  }
}
