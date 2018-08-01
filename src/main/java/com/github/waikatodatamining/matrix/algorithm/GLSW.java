package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.transformation.Center;

/**
 * Generalized Least Squares Weighting (GLSW) as described in
 * <a href="http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#GLS_Weighting_and_EPO">GLS Weighting</a>
 * <p>
 * Parameter alpha controls the downweights interferences.
 *
 * Parameters:
 * - alpha: Defines how strongly GLSW downweights interferences
 *
 * @author Steven Lang
 */
public class GLSW extends AbstractAlgorithm implements Filter {

  private static final long serialVersionUID = -7474573037658789063L;

  /** Alpha parameter. Defines how strongly GLSW downweights interferences */
  protected double m_Alpha;

  /**
   * Get the constructed projection matrix.
   *
   * @return Projection matrix G
   */
  public Matrix getProjectionMatrix() {
    return m_G;
  }

  /** Projection Matrix */
  protected Matrix m_G;

  public double getAlpha() {
    return m_Alpha;
  }

  /**
   * Set the alpha parameter. Defines how strongly GLSW downweights
   * interferences. Larger values (> 0.001) decreases the filtering effect.
   * Smaller values (< 0.001) increase the filtering effect.
   *
   * @param alpha Alpha parameter
   */
  public void setAlpha(double alpha) {
    if (alpha <= 0) {
      m_Logger.warning("Alpha must be > 0 but was " + alpha + ".");
    }
    else {
      m_Alpha = alpha;
      reset();
    }
  }

  @Override
  protected void reset() {
    super.reset();
    m_G = null;
  }

  @Override
  protected void initialize() {
    super.initialize();
    m_Alpha = 1e-3;
  }

  @Override
  public String toString() {
    if (m_Initialized) {
      return "Generalized Least Squares Weighting. Projection Matrix shape: " + m_G.shapeString();
    }
    else {
      return "Generalized Least Squares Weighting. Model not yet initialized.";
    }
  }

  /**
   * Initializes using the provided data.
   *
   * @param x1 the input data
   * @param x2 the dependent variable(s)
   * @return null if successful, otherwise error message
   */
  public String initialize(Matrix x1, Matrix x2) {
    String result;

    // Always work on copies
    x1 = x1.copy();
    x2 = x2.copy();

    reset();

    result = check(x1, x2);

    if (result == null) {
      result = doInitialize(x1, x2);
      m_Initialized = (result == null);
    }

    return result;
  }

  public String doInitialize(Matrix x1, Matrix x2) {
    super.initialize();


    Matrix C = getCovarianceMatrix(x1, x2);

    // SVD
    Matrix V = getEigenvectorMatrix(C);
    Matrix D = getWeightMatrix(C);

    // Projection Matrix
    m_G = V.mul(D.inverse()).mul(V.t());

    return null;
  }

  protected Matrix getEigenvectorMatrix(Matrix C) {
    return C.getEigenvalueDecompositionV();
  }

  protected Matrix getWeightMatrix(Matrix C) {
    // Get eigenvalues
    Matrix Ssquared = C.getEigenvalueDecompositionD();

    // Weights
    Matrix D = Ssquared.div(m_Alpha);
    D = D.add(MatrixFactory.eyeLike(D));
    D = D.sqrt();
    return D;
  }

  protected Matrix getCovarianceMatrix(Matrix x1, Matrix x2) {
    // Center X1, X2
    Center c1 = new Center();
    Center c2 = new Center();
    Matrix x1Centered = c1.transform(x1);
    Matrix x2Centered = c2.transform(x2);

    // Build difference
    Matrix Xd = x2Centered.sub(x1Centered);

    // Covariance Matrix
    return Xd.t().mul(Xd);
  }


  protected Matrix doTransform(Matrix predictors) {
    return predictors.mul(m_G);
  }

  @Override
  public Matrix transform(Matrix predictors) {
    if (!isInitialized())
      throw new IllegalStateException("Algorithm hasn't been initialized!");

    return doTransform(predictors);
  }

  /**
   * Hook method for checking the data before training.
   *
   * @param x1 first sample set
   * @param x2 second sample set
   * @return null if successful, otherwise error message
   */
  protected String check(Matrix x1, Matrix x2) {
    if (x1 == null)
      return "No x1 matrix provided!";
    if (x2 == null)
      return "No x2 matrix provided!";
    if (x1.numRows() != x2.numRows() || x1.numColumns() != x2.numColumns()) {
      return "Matrices x1 and x2 must have the same shape";
    }
    return null;
  }
}
