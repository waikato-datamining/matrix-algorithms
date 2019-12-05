package com.github.waikatodatamining.matrix.algorithms.pls.kernel;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Linear Kernel.
 * <p>
 * K(x_i,y_j)=x_i^T*y_j
 * or also
 * K(X, Y)=X*Y^T
 *
 * @author Steven Lang
 */
public class LinearKernel extends AbstractKernel {

  private static final long serialVersionUID = 841527107134287683L;

  @Override
  public double applyVector(Matrix x, Matrix y) {
    return x.vectorDot(y);
  }

  @Override
  public Matrix applyMatrix(Matrix X, Matrix Y) {
    return X.mul(Y.transpose());
  }

  @Override
  public Matrix applyMatrix(Matrix X) {
    return this.applyMatrix(X, X);
  }

  @Override
  public String toString() {
    return "Linear Kernel: K(x,y)=x^T*y";
  }
}
