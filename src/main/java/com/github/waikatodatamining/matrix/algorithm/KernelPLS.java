package com.github.waikatodatamining.matrix.algorithm;

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.transformation.Center;
import com.github.waikatodatamining.matrix.transformation.kernel.AbstractKernel;
import com.github.waikatodatamining.matrix.transformation.kernel.RBFKernel;

/**
 * Kernel Partial Least Squares algorithm.
 * <br>
 * See here:
 * <a href="http://www.jmlr.org/papers/volume2/rosipal01a/rosipal01a.pdf">Kernel Partial Least Squares Regression in Reproducing
 * Kernel Hilbert Space</a>
 *
 * @author Steven Lang
 */
public class KernelPLS extends AbstractMultiResponsePLS {
    private static final long serialVersionUID = -2760078672082710402L;
    public static final int SEED = 0;

    protected Matrix m_K;
    protected Matrix m_T;
    protected Matrix m_T_trans;
    protected Matrix m_U;
    protected Matrix m_B;
    protected Matrix m_X;
    protected Matrix m_X_trans;
    protected Matrix m_Y;
    protected Matrix m_Y_trans;
    protected AbstractKernel m_Kernel;
    protected double m_Tol;
    protected int m_MaxIter;
    protected Center m_CenterTransform;

    @Override
    protected void initialize() {
        super.initialize();
        setKernel(new RBFKernel());
        setTol(1e-6);
        setMaxIter(500);
        m_CenterTransform = new Center();
    }

    public AbstractKernel getKernel() {
        return m_Kernel;
    }

    public void setKernel(AbstractKernel kernel) {
        this.m_Kernel = kernel;
    }

    public int getMaxIter() {
        return m_MaxIter;
    }

    public void setMaxIter(int maxIter) {
        this.m_MaxIter = maxIter;
    }

    public double getTol() {
        return m_Tol;
    }

    public void setTol(double tol) {
        this.m_Tol = tol;
    }

    @Override
    protected int getMinColumnsResponse() {
        return 1;
    }

    @Override
    protected int getMaxColumnsResponse() {
        return -1;
    }

    @Override
    protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
        Matrix I, t, u, c;


        // Init
        int numComponents = getNumComponents();
        m_X = predictors;
        m_X_trans = m_X.transpose();
        m_Y = response;
        int numRows = m_X.getRowDimension();
        int numClasses = m_Y.getColumnDimension();
        int numFeatures = m_X.getColumnDimension();

        c = new Matrix(numClasses, 1);

        u = MatrixHelper.randn(numRows, 1, SEED);
        t = new Matrix(numRows, 1);
        I = Matrix.identity(numRows, numRows);

        m_T = new Matrix(numRows, numComponents);
        m_U = new Matrix(numRows, numComponents);
        m_B = new Matrix(numRows, numClasses);
        m_K = m_Kernel.applyMatrix(m_X, m_X);
        m_K = centralizeInKernelSpace(m_K);

        for (int currentComponent = 0; currentComponent < numComponents; currentComponent++) {
            int iterations = 0;
            Matrix tOld = t;
            // Repeat 1) - 3) until convergence: either change of m_T is lower than m_Tol or maximum
            // number of iterations has been reached (m_MaxIter)
            while (true) {
                // 1)
                t = m_K.times(u);
                MatrixHelper.normalizeVector(t);

                // 2)
                c = m_Y.transpose().times(t);

                // 3)
                u = m_Y.times(c);
                MatrixHelper.normalizeVector(u);

                Matrix tDiff = t.minus(tOld);
                double change = tDiff.transpose().times(tDiff).get(0, 0);
                if (change < m_Tol || iterations >= m_MaxIter) {
                    break;
                } else {
                    tOld = t;
                    iterations++;
                }
            }

            // Deflate
            Matrix ttTrans = t.times(t.transpose());
            Matrix part = I.minus(ttTrans);
            m_K = part.times(m_K).times(part);
            m_Y = m_Y.minus(ttTrans.times(m_Y));

            // Store u,t
            MatrixHelper.setColumnVector(t, m_T, currentComponent);
            MatrixHelper.setColumnVector(u, m_U, currentComponent);
        }

        m_T_trans = m_T.transpose();
        return null;
    }

    /**
     * Centralize a kernel matrix in the kernel space via:
     * K <- (I - 1/n * 1_n * 1_n^T) * K * (I - 1/n * 1_n * 1_n^T)
     *
     * @param K Kernel matrix
     * @return Centralised kernel matrix
     */
    protected Matrix centralizeInKernelSpace(Matrix K) {
        int n = m_X.getRowDimension();
        Matrix I = Matrix.identity(n, n);
        Matrix one = new Matrix(n, 1, 1.0);

        // Centralize in kernel space
        Matrix part = (I.minus(one.times(one.transpose()).times(1.0 / n)));
        return part.times(K).times(part);
    }

    @Override
    protected Matrix doPerformPredictions(Matrix predictors) {
        Matrix K_t = m_Kernel.applyMatrix(predictors, m_X);

        // Y_hat = K_t * U (T^T * K * U)^-1 * T^T * Y
        Matrix Y_hat = K_t.times(m_U).times(m_T_trans.times(m_K).times(m_U).inverse()).times(m_T_trans).times(m_Y);

        return Y_hat;
    }

    @Override
    public String[] getMatrixNames() {
        return new String[]{"K", "T", "U", "Y"};
    }

    @Override
    public Matrix getMatrix(String name) {
        switch (name) {
            case "K":
                return m_K;
            case "T":
                return m_T;
            case "U":
                return m_U;
            case "Y":
                return m_Y;
        }
        return null;
    }

    @Override
    public boolean hasLoadings() {
        return true;
    }

    @Override
    public Matrix getLoadings() {
        return m_T;
    }

    @Override
    public boolean canPredict() {
        return true;
    }

    @Override
    protected Matrix doTransform(Matrix predictors) throws Exception {

        Matrix K_t = m_Kernel.applyMatrix(predictors, m_X);
        K_t = centralizeInKernelSpace(K_t);

        return K_t.times(m_U);
    }
}
