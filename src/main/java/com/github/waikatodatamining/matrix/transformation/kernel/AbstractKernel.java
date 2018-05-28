package com.github.waikatodatamining.matrix.transformation.kernel;


import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;

import java.io.Serializable;

/**
 * Abstract kernel class. Implementations represent kernels that compute a dot product of two given
 * vectors in the kernel space (see {@link AbstractKernel#applyVector(Matrix, Matrix)}).
 * That is: K(x,y) = phi(x)*phi(y)
 *
 * @author Steven Lang
 */
public abstract class AbstractKernel implements Serializable {
    private static final long serialVersionUID = 8820493548875411535L;

    /**
     * Compute the dot product of the mapped x and y vectors in the kernel space, that is:
     * K(x,y) = phi(x)*phi(y)
     *
     * @param x First vector
     * @param y Second vector
     * @return Dot product of the given vector in the kernel space
     */
    public abstract double applyVector(Matrix x, Matrix y);

    /**
     * Create a matrix K that consists of entries K_i,j = K(x_i,y_j) = phi(x_i)*phi(y_j)
     *
     * @param X First matrix
     * @param Y Second matrix
     * @return Matrix K with K_i,j = K(x_i,y_j) = phi(x_i)*phi(y_j)
     */
    public Matrix applyMatrix(Matrix X, Matrix Y) {
        Matrix result = new Matrix(X.getRowDimension(), Y.getRowDimension());
        for (int i = 0; i < X.getRowDimension(); i++) {
            for (int j = 0; j < Y.getRowDimension(); j++) {
                Matrix rowI = MatrixHelper.rowAsVector(X, i);
                Matrix rowJ = MatrixHelper.rowAsVector(Y, j);
                result.set(i, j, applyVector(rowI, rowJ));
            }
        }
        return result;
    }
}
