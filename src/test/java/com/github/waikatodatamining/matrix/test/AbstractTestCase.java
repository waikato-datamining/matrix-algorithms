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
 * AbstractTestCase.java
 * Copyright (C) 2010-2018 University of Waikato, Hamilton, New Zealand
 */
package com.github.waikatodatamining.matrix.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import java.util.HashSet;

/**
 * Ancestor for all test cases.
 * <br><br>
 * Any regression test can be skipped as follows: <br>
 *   <code>-Dmatrix.test.noregression=true</code>
 * <br><br>
 * Individual tests can be skipped as follows (comma-separated lost): <br>
 *   <code>-Dmatrix.test.skip=some.where.Class1Test,adams.some.where.else.Class2Test</code>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractTestCase
  extends TestCase {

  /** property indicating whether regression tests should not be executed. */
  public final static String PROPERTY_NOREGRESSION = "matrix.test.noregression";

  /** property listing all test classes that should not get executed. */
  public final static String PROPERTY_SKIP = "matrix.test.skip";

  /** whether to execute any regression test. */
  protected boolean m_NoRegressionTest;

  /** the helper class for regression. */
  protected Regression m_Regression;

  /** the test class to use. */
  protected TestHelper m_TestHelper;

  /** the classnames of tests to skip. */
  protected HashSet<String> m_SkipTests;

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractTestCase(String name) {
    super(name);
  }

  /**
   * Tries to load the class based on the test class's name.
   *
   * @return		the class that is being tested or null if none could
   * 			be determined
   */
  protected Class getTestedClass() {
    Class	result;

    result = null;

    if (getClass().getName().endsWith("Test")) {
      try {
	result = Class.forName(getClass().getName().replaceAll("Test$", ""));
      }
      catch (Exception e) {
	result = null;
      }
    }

    return result;
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs.
   */
  @Override
  protected void setUp() throws Exception {
    Class	cls;
    String	skipped;
    String[]	parts;

    super.setUp();

    // SSL handling
    System.setProperty("jsse.enableSNIExtension", "false");

    // any tests that are skipped?
    m_SkipTests = new HashSet<>();
    skipped     = System.getProperty(PROPERTY_SKIP);
    if ((skipped != null) && !skipped.trim().isEmpty()) {
      parts = skipped.trim().replace(" ", "").split(",");
      for (String part: parts) {
	if (!part.trim().isEmpty())
	  m_SkipTests.add(part);
      }
    }

    cls = getTestedClass();
    if (cls != null)
      m_Regression = new Regression(cls);

    m_TestHelper       = newTestHelper();
    m_NoRegressionTest = Boolean.getBoolean(PROPERTY_NOREGRESSION);
  }

  /**
   * Override to run the test and assert its state. Checks whether the test
   * or test-method is platform-specific.
   * 
   * @throws Throwable if any exception is thrown
   */
  @Override
  protected void runTest() throws Throwable {
    String		msg;

    msg = null;

    if (m_SkipTests.contains(getClass().getName()))
      msg = "Test excluded from being run (" + getClass().getName() + ")";
    
    if (msg == null)
      super.runTest();
    else
      System.out.println("Skipped: " + msg);
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  @Override
  protected void tearDown() throws Exception {
    m_Regression = null;

    super.tearDown();
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  protected TestHelper newTestHelper() {
    return new TestHelper(this, "");
  }

  /**
   * Runs the specified suite. Used for running the test from commandline.
   *
   * @param suite	the suite to run
   */
  public static void runTest(Test suite) {
    TestRunner.run(suite);
  }
}
