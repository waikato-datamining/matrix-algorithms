package com.github.waikatodatamining.matrix.test.regression;


import java.io.File;
import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Class representing an abstract regression test regarding a single reference
 * file.
 * <p>
 *
 * @param <T> Object type that is checked for regression
 * @author Steven Lang
 */
public abstract class AbstractRegression<T> {

  /** Tolerance */
  protected final static double EPSILON = 1e-6;

  /** Regression path */
  private String m_path;

  /** Actual result */
  private T m_actual;

  /** Expected result */
  private T m_expected;

  /**
   * Constructor with regression path for reference file and actual result which
   * will be compared to the expected result.
   *
   * @param path   Path to reference file
   * @param actual Actual result (will be compared to expected result)
   */
  public AbstractRegression(String path, T actual) {
    this.m_path = path + "." + getFilenameExtension();
    this.m_actual = actual;
    loadExpected();
  }

  /**
   * Ensures that all parent directories of the given path exist.
   *
   * @param path Path to the reference file
   */
  private static void ensureDirExists(String path) {
    String subpath = path.substring(0, path.lastIndexOf('/'));
    new File(subpath).mkdirs();
  }

  /**
   * Get the reference path for this regression.
   *
   * @return Reference path
   */
  public String getPath() {
    return m_path;
  }

  /** Check the actual and expected results for equality */
  protected abstract void checkEquals(T expected, T actual);

  /** Run all assertions */
  public void runAssertions() {
    checkEquals(m_expected, m_actual);
  }

  /**
   * Read the expected result from the reference path
   *
   * @param path Reference file path
   * @return Expected result/Reference result
   * @throws Exception File could not be read
   */
  protected abstract T readExpected(String path) throws Exception;

  /**
   * Write the expected result in the case when no expected result could be found.
   *
   * @param path     Reference file path
   * @param expected Expected object
   * @throws Exception Could not write file
   */
  protected abstract void writeExpected(String path, T expected) throws Exception;

  /** Loads the expected results */
  private void loadExpected() {
    try {
      m_expected = readExpected(m_path);
    }
    catch (NoSuchFileException nsfe) {
      System.out.println("File <" + m_path + "> does not exist yet. Creating new reference.");
      createNewReference(m_path);
      m_expected = m_actual;
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Failed to load reference file: " + m_path);
    }
  }

  /**
   * Create new reference file.
   *
   * @param path File path to store the reference at
   */
  private void createNewReference(String path) {
    try {
      ensureDirExists(path);
      writeExpected(path, m_actual);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("Failed to create new reference file: " + path);
    }
  }

  /**
   * Get the filename extension. E.g. one of [txt, csv, dat, ref, ...]
   *
   * @return Filename extension
   */
  protected abstract String getFilenameExtension();
}
