package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.algorithm.api.Filter;
import com.github.waikatodatamining.matrix.algorithm.api.ResponseFilter;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.transformation.Center;

/**
 * http://www.cs.columbia.edu/~stratos/research/pca_cca.pdf
 */
public class CCAFilter extends AbstractAlgorithm implements Filter, ResponseFilter {

  private static final long serialVersionUID = 5252111378504552170L;


  /** X Ridge regression parameter */
  protected double m_lambdaX;

  /** Y Ridge regression parameter */
  protected double m_lambdaY;

  /** Target dimension */
  protected int m_kcca;

  /** Maximum number of iterations */
  protected int m_maxIter;

  /** Center X */
  protected Center m_centerX;

  /** Center Y */
  protected Center m_centerY;

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
      m_Logger.warning("Target dimension kcca must be > 0 but was " + kcca + ".");
    }
    else {
      m_kcca = kcca;
      reset();
    }
  }

  /**
   * Get maximum number of iterations.
   *
   * @return Maximum number of iterations
   */
  public int getMaxIter() {
    return m_maxIter;
  }

  /**
   * Set maximum number of iterations. Must be > 0.
   *
   * @param maxIter Maximum number of iterations
   */
  public void setMaxIter(int maxIter) {
    if (maxIter < 1) {
      m_Logger.warning("Maximum number of iterations must be > 0 but was " + maxIter + ".");
    }
    else {
      m_maxIter = maxIter;
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
  protected void reset() {
    super.reset();
    m_ProjX = null;
    m_ProjY = null;
  }

  @Override
  protected void initialize() {
    super.initialize();
    m_kcca = 1;
    m_lambdaX = 1e-2;
    m_lambdaY = 1e-2;
    m_centerX = new Center();
    m_centerY = new Center();
  }

  /**
   * Initializes using the provided data.
   *
   * @param x the input data
   * @param y the dependent variable(s)
   * @return null if successful, otherwise error message
   */
  public String initialize(Matrix x, Matrix y) {
    String result;

    // Always work on copies
    x = x.copy();
    y = y.copy();

    reset();

    result = check(x, y);

    if (result == null) {
      result = doInitialize(x, y);
      m_Initialized = (result == null);
    }

    return result;
  }

  protected String doInitialize(Matrix X, Matrix Y) {
    super.initialize();

    int numFeatures = X.numColumns();
    int numTargets = Y.numColumns();

    // Check if dimension m_kcca is valid
    if (m_kcca > Math.min(numFeatures, numTargets)) {
      return "Projection dimension must be <= " +
	"min(X.numColumns, Y.numColumns).";
    }

    // Center input
    X = m_centerX.transform(X);
    Y = m_centerY.transform(Y);

    // Regularization matrices
    Matrix lambdaIX = MatrixFactory.eye(numFeatures).mul(m_lambdaX);
    Matrix lambdaIY = MatrixFactory.eye(numTargets).mul(m_lambdaY);

    // Get covariance matrices
    Matrix Cxx = X.t().mul(X).add(lambdaIX);
    Matrix Cyy = Y.t().mul(Y).add(lambdaIY);
    Matrix Cxy = X.t().mul(Y);

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

    return null;
  }


  protected Matrix powMinusHalf(Matrix A) {
    Matrix eigValsDesc = A.getEigenvaluesSortedDescending();
    Matrix eigVecsDesc = A.getEigenvectors(true);
    Matrix diag = MatrixFactory.diag(eigValsDesc);
    Matrix DsqrtInv = diag.sqrt().inverse();
    Matrix ApowHalf = eigVecsDesc.mul(DsqrtInv).mul(eigVecsDesc.t());
    return ApowHalf;
  }


  /**
   * Hook method for checking the data before training.
   *
   * @param x first sample set
   * @param y second sample set
   * @return null if successful, otherwise error message
   */
  protected String check(Matrix x, Matrix y) {
    if (x == null)
      return "No x matrix provided!";
    if (y == null)
      return "No y matrix provided!";
    return null;
  }

  /**
   * Transforms the predictors data.
   *
   * @param predictors the input data
   * @throws Exception if analysis fails
   * @return the transformed data
   */
  protected Matrix doTransform(Matrix predictors) throws Exception {
    predictors = m_centerX.transform(predictors);
    return predictors.mul(m_ProjX);
  }

  /**
   * Transforms the response data.
   *
   * @param response the input data
   * @throws Exception if analysis fails
   * @return the transformed data
   */
  protected Matrix doTransformResponse(Matrix response) throws Exception {
    response = m_centerY.transform(response);
    return response.mul(m_ProjY);
  }

  @Override
  public Matrix transform(Matrix predictors) throws Exception {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doTransform(predictors);
  }

  @Override
  public Matrix transformResponse(Matrix response) throws Exception {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doTransformResponse(response);
  }
}
