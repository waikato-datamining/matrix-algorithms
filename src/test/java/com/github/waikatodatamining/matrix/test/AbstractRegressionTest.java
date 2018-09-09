package com.github.waikatodatamining.matrix.test;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;
import com.github.waikatodatamining.matrix.test.misc.TestRegression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractRegressionTest<T> {

  /** Regression TAG */
  public final static String REGRESSION_TAG = "regression-method";

  /** TestName reference */
  public String m_testName;

  /** Regression input matrices */
  protected Matrix[] m_inputData;

  /** Regression manager */
  protected RegressionManager m_regressionManager;

  /** Subject to test */
  protected T m_subject;

  @TestRegression
  protected void defaultSetup() {
    // Keep body empty and do not set any parameters and use default values
  }

  /**
   * Setup each test by doing the following:
   * - Load the input test data
   * - Instantiate the algorithm object
   * - Retrieve the test name/ current method name
   */
  @BeforeEach
  public void beforeEach(TestInfo testInfo) {
    m_inputData = getInputData();
    m_subject = instantiateSubject();
    m_testName = testInfo.getDisplayName().replaceAll("\\(\\)", "");
    m_regressionManager = new RegressionManager(getReferenceDir(), m_testName);
  }

  @AfterEach
  public void afterEach(TestInfo testInfo) {
    // Check if the test contains the regression tag
    if (testInfo.getTags().contains(REGRESSION_TAG)) {
      runRegression();
    }
  }

  /**
   * Setup the given regressions. First run the algorithm on the data. Then
   * add all components of the model via {@link AbstractRegressionTest#addRegression(String, Double)} or other add methods.
   *
   * @param subject   Algorithm to run
   * @param inputData Input data
   */
  protected abstract void setupRegressions(T subject, Matrix[] inputData) throws Exception;

  /**
   * Get the input datasets used for the algorithm tests.
   *
   * @return Paths to input data
   */
  protected abstract TestDataset[] getDatasets();

  /** Create an instance of the subject */
  protected abstract T instantiateSubject();

  /**
   * Run the set-up algorithm.
   */
  protected void runRegression() {
    try {
      setupRegressions(m_subject, m_inputData);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Setting up regression group failed.");
    }

    // Run all regression assertions
    m_regressionManager.runAssertions();
  }

  /**
   * Get the input data.
   *
   * @return Input data
   */
  private Matrix[] getInputData() {
    return Arrays.stream(getDatasets())
      .map(TestDataset::load)
      .toArray(Matrix[]::new);
  }

  /**
   * Get reference file directory.
   * Constructed based on the package name and removes "Test" at each class name.
   *
   * @return Reference file directory
   */
  protected String getReferenceDir() {
    String path = getClass().getName()
      .replaceAll("\\.", File.separator) // Replace "." with "/"
      .replaceAll("Test", ""); // Remove "Test"
    return Paths.get("src/test/resources", path).toString();
  }

  /**
   * Add a new Matrix with a tag to the regression manager.
   *
   * @param tag    Tag for the matrix
   * @param matrix Matrix to check with the reference
   */
  protected void addRegression(String tag, Matrix matrix) {
    m_regressionManager.add(tag, matrix);
  }

  /**
   * Add a new Double with a tag to the regression manager.
   *
   * @param tag   Tag for the double value
   * @param value Double to check with the reference
   */
  protected void addRegression(String tag, Double value) {
    m_regressionManager.add(tag, value);
  }
}
