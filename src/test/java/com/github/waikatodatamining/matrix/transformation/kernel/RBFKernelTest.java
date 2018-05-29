package com.github.waikatodatamining.matrix.transformation.kernel;

import com.github.waikatodatamining.matrix.transformation.Center;
import com.github.waikatodatamining.matrix.transformation.CenterTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class RBFKernelTest extends AbstractKernelTest {

    /**
     * Constructs the test case. Called by subclasses.
     *
     * @param name the name of the test
     */
    public RBFKernelTest(String name) {
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
    protected RBFKernel[] getRegressionSetups() {
        return new RBFKernel[]{
                new RBFKernel()
        };
    }

    /**
     * Returns the test suite.
     *
     * @return		the suite
     */
    public static Test suite() {
        return new TestSuite(RBFKernelTest.class);
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
