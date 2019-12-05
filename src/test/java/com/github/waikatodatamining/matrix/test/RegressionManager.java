package com.github.waikatodatamining.matrix.test;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.test.regression.AbstractRegression;
import com.github.waikatodatamining.matrix.test.regression.DoubleRegression;
import com.github.waikatodatamining.matrix.test.regression.MatrixRegression;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Group of regressions. Contains all parameters/outputs/model-matrices that
 * need to be checked for the regression tests of a single model configuration.
 * <p>
 * Each object that should be checked has to be added via one of the #add
 * methods.
 *
 * @author Steven Lang
 */
public class RegressionManager {

  /** Base directory in which the regression reference files are stored */
  protected String m_referenceDir;

  /** All regressions in this group */
  protected List<AbstractRegression> m_regressions;

  /** Regression test name */
  protected String m_testName;

  /**
   * Creates a regression group that stores and loads files in a certain
   * reference directory.
   *
   * @param referenceDir Reference directory
   * @param testName     Test name (file suffixes)
   */
  RegressionManager(String referenceDir, String testName) {
    this.m_referenceDir = referenceDir;
    this.m_testName = testName;
    m_regressions = new ArrayList<>();
  }

  /**
   * Add a new Matrix with a tag to the regression group.
   *
   * @param tag    Tag for the matrix
   * @param matrix Matrix to check with the reference
   */
  public void add(String tag, Matrix matrix) {
    String path = constructPath(tag);
    MatrixRegression regression = new MatrixRegression(path, matrix);
    m_regressions.add(regression);
  }

  /**
   * Add a new Double with a tag to the regression group.
   *
   * @param tag   Tag for the double value
   * @param value Double to check with the reference
   */
  public void add(String tag, Double value) {
    String path = constructPath(tag);
    DoubleRegression regression = new DoubleRegression(path, value);
    m_regressions.add(regression);
  }

  /** Construct the regression path from a given tag */
  private String constructPath(String tag) {
    return Paths.get(m_referenceDir, m_testName, tag).toString();
  }

  /** Run regression assertions */
  public void runAssertions() {
    for (AbstractRegression reg : m_regressions) {
      reg.runAssertions();
    }
  }
}
