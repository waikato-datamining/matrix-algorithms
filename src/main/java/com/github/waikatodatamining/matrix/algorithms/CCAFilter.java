package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.algorithm.SupervisedMatrixAlgorithmWithResponseTransform;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;

/**
 * Canonical Correlation Analysis Filter.
 * <p>
 * http://www.cs.columbia.edu/~stratos/research/pca_cca.pdf
 * <p>
 * Parameters:
 * - lambdaX: Ridge regularization parameter for X
 * - lambdaY: Ridge regularization parameter for Y
 * - kcca: Projection output dimension
 *
 * @author Steven Lang
 */
public class CCAFilter
  extends SupervisedMatrixAlgorithmWithResponseTransform {

  private static final long serialVersionUID = 5252111378504552170L;

  /** X Ridge regression parameter */
  protected double m_lambdaX = 1e-2;

  /** Y Ridge regression parameter */
  protected double m_lambdaY = 1e-2;

  /** Target dimension */
  protected int m_kcca = 1;

  /** Center X */
  protected Center m_centerX = new Center();

  /** Center Y */
  protected Center m_centerY = new Center();

  /** X projection matrix */
  protected Matrix m_ProjX;

  /** Y projection matrix */
  protected Matrix m_ProjY;

  /**
   * Get target dimension.
   *
   * @return Target dimension
   */
  public int getKcca() {
    return m_kcca;
  }

  /**
   * Set target dimension. Must be > 0.
   *
   * @param kcca Target dimension
   */
  public void setKcca(int kcca) {
    if (kcca < 1) {
      getLogger().warning("Target dimension kcca must be > 0 but was " + kcca + ".");
    }
    else {
      m_kcca = kcca;
      reset();
    }
  }

  /**
   * Get lambda X regularization parameter.
   *
   * @return Lambda regularization parameter
   */
  public double getLambdaX() {
    return m_lambdaX;
  }

  /**
   * Set the lambda X parameter.
   *
   * @param lambdaX Lambda X parameter
   */
  public void setLambdaX(double lambdaX) {
    m_lambdaX = lambdaX;
    reset();
  }

  /**
   * Get lambda Y regularization parameter.
   *
   * @return Lambda regularization parameter
   */
  public double getLambdaY() {
    return m_lambdaY;
  }

  /**
   * Set the lambda Y parameter.
   *
   * @param lambdaY Lambda Y parameter
   */
  public void setLambdaY(double lambdaY) {
    m_lambdaY = lambdaY;
    reset();
  }

  /**
   * Get X projection matrix.
   *
   * @return X projection matrix
   */
  public Matrix getProjectionMatrixX() {
    return m_ProjX;
  }

  /**
   * Get Y projection matrix.
   *
   * @return Y projection matrix
   */
  public Matrix getProjectionMatrixY() {
    return m_ProjY;
  }

  @Override
  public String toString() {
    return "Canonical Correlation Analysis Filter (CCARegression)";
  }

  @Override
  protected void doReset() {
    m_ProjX = null;
    m_ProjY = null;
    m_centerX.reset();
    m_centerY.reset();
  }

  @Override
  protected void doConfigure(Matrix X, Matrix y) {
    int numFeatures = X.numColumns();
    int numTargets = y.numColumns();

    // Check if dimension m_kcca is valid
    if (m_kcca > Math.min(numFeatures, numTargets)) {
      throw new MatrixAlgorithmsException("Projection dimension must be <= " +
            "min(X.numColumns, Y.numColumns).");
    }

    // Center input
    X = m_centerX.configureAndTransform(X);
    y = m_centerY.configureAndTransform(y);

    // Regularization matrices
    Matrix lambdaIX = MatrixFactory.eye(numFeatures).mul(m_lambdaX);
    Matrix lambdaIY = MatrixFactory.eye(numTargets).mul(m_lambdaY);

    // Get covariance matrices
    Matrix Cxx = X.t().mul(X).add(lambdaIX);
    Matrix Cyy = y.t().mul(y).add(lambdaIY);
    Matrix Cxy = X.t().mul(y);

    // Apply A^(-1/2)
    Matrix CxxInvSqrt = powMinusHalf(Cxx);
    Matrix CyyInvSqrt = powMinusHalf(Cyy);

    // Calculate omega for SVD
    Matrix omega = CxxInvSqrt.mul(Cxy).mul(CyyInvSqrt);

    Matrix U = omega.svdU().normalized(0); // Left singular vectors
    Matrix V = omega.svdV().normalized(0); // Right singular vectors

    Matrix C = U.getSubMatrix(0, U.numRows(), 0, m_kcca);
    Matrix D = V.getSubMatrix(0, V.numRows(), 0, m_kcca);


    m_ProjX = CxxInvSqrt.mul(C);
    m_ProjY = CyyInvSqrt.mul(D);
  }


  /**
   * Compute A^(-1/2) = (A^(-1))^(1/2) on a matrix A, where A^(1/2) = M with
   * A = MM.
   *
   * @param A Input matrix
   * @return A^(-1/2)
   */
  protected Matrix powMinusHalf(Matrix A) {
    Matrix eigValsDesc = A.getEigenvaluesSortedDescending();
    Matrix eigVecsDesc = A.getEigenvectors(true);
    Matrix diag = MatrixFactory.diag(eigValsDesc);
    Matrix DsqrtInv = diag.sqrt().inverse();
    Matrix ApowHalf = eigVecsDesc.mul(DsqrtInv).mul(eigVecsDesc.t());
    return ApowHalf;
  }


  /**
   * Transforms the predictors data.
   *
   * @param predictors the input data
   * @return the transformed data
   */
  @Override
  protected Matrix doTransform(Matrix predictors) {
    predictors = m_centerX.transform(predictors);
    return predictors.mul(m_ProjX);
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }

  /**
   * Transforms the response data.
   *
   * @param response the input data
   * @return the transformed data
   */
  @Override
  protected Matrix doTransformResponse(Matrix response) {
    response = m_centerY.transform(response);
    return response.mul(m_ProjY);
  }

}
