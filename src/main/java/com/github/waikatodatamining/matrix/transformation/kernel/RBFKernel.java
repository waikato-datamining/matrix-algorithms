package com.github.waikatodatamining.matrix.transformation.kernel;

import Jama.Matrix;
import static com.github.waikatodatamining.matrix.core.MatrixHelper.l2VectorNorm;

/**
 * Radial Basis Function Kernel.
 * <p>
 * K(x,y) = exp(-1*||x - y||^2/(2*sigma^2))
 * or
 * K(x,y) = exp(-1*gamma*||x - y||^2), with gamma=2*sigma^2
 *
 * @author Steven Lang
 */
public class RBFKernel extends AbstractKernel {

    private static final long serialVersionUID = -5801833711201856600L;

    /**
     * Gamma parameter
     */
    protected double m_Gamma = 1;

    /**
     * Get the gamma parameter.
     * K(x,x') = exp(-1*gamma*||x - x'||^2)
     *
     */
    public double getGamma() {
        return m_Gamma;
    }

    /**
     * Set the gamma parameter.
     * K(x,x') = exp(-1*gamma*||x - x'||^2)
     *
     * @param gamma Gamma parameter
     */
    public void setGamma(double gamma) {
        this.m_Gamma = gamma;
    }

    @Override
    public double applyVector(Matrix x, Matrix y) {
        double norm2 = l2VectorNorm(x.minus(y));
        if (Double.isNaN(m_Gamma)) {
            m_Gamma = 1.0 / x.getRowDimension();
        }
        return StrictMath.exp(-1 * m_Gamma * norm2);
    }

    @Override
    public String toString() {
        return String.format("RBF Kernel: K(x,y) = exp(-1*gamma*||x - y||^2), gamma=%f", m_Gamma);
    }
}
