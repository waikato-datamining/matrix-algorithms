package com.github.waikatodatamining.matrix.transformation.kernel;

import Jama.Matrix;

/**
 * Linear Kernel.
 * <p>
 * K(x,y)=x^T*y
 *
 * @author Steven Lang
 */
public class LinearKernel extends AbstractKernel {
    private static final long serialVersionUID = 841527107134287683L;

    @Override
    public double applyVector(Matrix x, Matrix y) {
        return x.transpose().times(y).get(0, 0);
    }

    @Override
    public String toString() {
        return "Linear Kernel: K(x,y)=x^T*y";
    }
}
