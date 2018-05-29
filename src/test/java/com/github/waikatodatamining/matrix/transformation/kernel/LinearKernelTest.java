package com.github.waikatodatamining.matrix.transformation.kernel;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LinearKernelTest extends AbstractKernelTest {

    /**
     * Constructs the test case. Called by subclasses.
     *
     * @param name the name of the test
     */
    public LinearKernelTest(String name) {
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
                "bolts.csv"
        };
    }

    /**
     * Returns the setups to use in the regression test.
     *
     * @return		the setups
     */
    @Override
    protected LinearKernel[] getRegressionSetups() {
        return new LinearKernel[]{
                new LinearKernel()
        };
    }

    /**
     * Returns the test suite.
     *
     * @return		the suite
     */
    public static Test suite() {
        return new TestSuite(LinearKernelTest.class);
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
