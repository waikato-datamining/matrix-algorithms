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

package com.github.waikatodatamining.matrix.algorithm.pls;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.PreprocessingType;
import com.github.waikatodatamining.matrix.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

import static com.github.waikatodatamining.matrix.core.MatrixFactory.*;

/**
 * Tests the DIPLS algorithm.
 *
 * @author Steven Lang
 */
public class DIPLSTest
  extends AbstractPLSTest<DIPLS> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public DIPLSTest(String name) {
    super(name);
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
      "bolts.csv",
    };
  }

  /**
   * Returns the filenames (without path) of the response data files to use
   * in the regression test.
   *
   * @return the filenames
   */
  @Override
  protected String[] getRegressionResponseFiles() {
    return new String[]{
      "bolts_response.csv",
      "bolts_response.csv",
      "bolts_response.csv",
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return the setups
   */
  @Override
  protected DIPLS[] getRegressionSetups() {
    DIPLS[] result;

    result = new DIPLS[3];
    result[0] = new DIPLS();
    result[0].setNumComponents(3);

    result[1] = new DIPLS();
    result[1].setNumComponents(3);
    result[1].setPreprocessingType(PreprocessingType.CENTER);
    result[1].setLambda(0.1);

    result[2] = new DIPLS();
    result[2].setNumComponents(3);
    result[2].setPreprocessingType(PreprocessingType.STANDARDIZE);


    return result;
  }


  /**
   * Compares the processed data against previously saved output data.
   */
  @Override
  public void testRegression() {
    Matrix dataInput;
    Matrix dataResponse;
    Matrix transformed;
    Matrix predicted;
    Matrix loadings;
    boolean ok;
    String regression;
    int i;
    String msg;
    String[] input;
    String[] response;
    DIPLS[] setups;
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

      msg = initializeUnsupervised(
        setups[i],
	dataInput,
	dataInput.add(randnLike(dataInput, 1, 2, i)),
	dataResponse);
      assertNull("Failed to initialize unsupervised with data?", msg);

      msg = initializeSupervised(
        setups[i],
	dataInput,
	dataInput.add(randnLike(dataInput, 1, 2, i)),
	dataResponse,
	dataResponse.add(randnLike(dataResponse, 1, 2, i)));
      assertNull("Failed to initialize supervised with data?", msg);

      msg = initializeSemiSupervised(
        setups[i],
	dataInput,
	dataInput.add(randnLike(dataInput, 1, 2, i)),
	dataInput.add(randnLike(dataInput, 1, 2, i*100)),
	dataResponse,
	dataResponse.add(randnLike(dataResponse, 1, 2, i)));
      assertNull("Failed to initialize semisupervised with data?", msg);

      transformed = process(dataInput, setups[i]);
      assertNotNull("Failed to transform data?", transformed);

      outputCurr = createOutputFilename(input[i], i, MatrixType.TRANSFORMED);
      output.add(outputCurr);
      ok = save(transformed, outputCurr);
      assertTrue("Failed to save regression data (transformed)?", ok);

      if (setups[i].canPredict()) {
	predicted = predict(dataInput, setups[i]);
	assertNotNull("Failed to predict?", predicted);

	outputCurr = createOutputFilename(input[i], i, MatrixType.PREDICTIONS);
	output.add(outputCurr);
	ok = save(predicted, outputCurr);
	assertTrue("Failed to save regression data (predicted)?", ok);
      }

      loadings = setups[i].getLoadings();
      if (loadings != null) {
	outputCurr = createOutputFilename(input[i], i, MatrixType.LOADINGS);
	output.add(outputCurr);
	ok = save(loadings, outputCurr);
	assertTrue("Failed to save regression data (loadings)?", ok);
      }
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
   * Unsupervised initialization.
   *
   * @param predictorsSourceDomain Predictors from source domain
   * @param predictorsTargetDomain Predictors from target domain
   * @param responseSourceDomain   Response from source domain
   * @return Result, null if no errors, else error String
   */
  protected String initializeUnsupervised(DIPLS scheme,
					  Matrix predictorsSourceDomain,
					  Matrix predictorsTargetDomain,
					  Matrix responseSourceDomain) {
    try {
      return scheme.initializeUnsupervised(
	predictorsSourceDomain,
	predictorsTargetDomain,
	responseSourceDomain);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + stackTraceToString(e));
      return null;
    }
  }

  /**
   * Supervised initialization.
   *
   * @param predictorsSourceDomain Predictors from source domain
   * @param predictorsTargetDomain Predictors from target domain
   * @param responseSourceDomain   Response from source domain
   * @param responseTargetDomain   Response from target domain
   * @return Result, null if no errors, else error String
   */
  protected String initializeSupervised(DIPLS scheme,
					Matrix predictorsSourceDomain,
					Matrix predictorsTargetDomain,
					Matrix responseSourceDomain,
					Matrix responseTargetDomain) {
    try {
      return scheme.initializeSupervised(
	predictorsSourceDomain,
	predictorsTargetDomain,
	responseSourceDomain,
	responseTargetDomain);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + stackTraceToString(e));
      return null;
    }
  }

  /**
   * Semisupervised initialization.
   *
   * @param predictorsSourceDomain          Predictors from source domain
   * @param predictorsTargetDomain          Predictors from target domain
   * @param predictorsTargetDomainUnlabeled Predictors from target domain
   *                                        without labels
   * @param responseSourceDomain            Response from source domain
   * @param responseTargetDomain            Response from target domain
   * @return Result, null if no errors, else error String
   */
  protected String initializeSemiSupervised(DIPLS scheme,
					    Matrix predictorsSourceDomain,
					    Matrix predictorsTargetDomain,
					    Matrix predictorsTargetDomainUnlabeled,
					    Matrix responseSourceDomain,
					    Matrix responseTargetDomain) {
    try {
      return scheme.initializeSemiSupervised(
	predictorsSourceDomain,
	predictorsTargetDomain,
	predictorsTargetDomainUnlabeled,
	responseSourceDomain,
	responseTargetDomain);
    }
    catch (Exception e) {
      fail("Failed to initialize: " + stackTraceToString(e));
      return null;
    }
  }


  /**
   * Returns the test suite.
   *
   * @return the suite
   */
  public static Test suite() {
    return new TestSuite(DIPLSTest.class);
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
