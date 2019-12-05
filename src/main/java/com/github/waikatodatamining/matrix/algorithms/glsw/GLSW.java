package com.github.waikatodatamining.matrix.algorithms.glsw;

import com.github.waikatodatamining.matrix.algorithms.Center;
import com.github.waikatodatamining.matrix.core.algorithm.SupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;

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
public class GLSW
  extends SupervisedMatrixAlgorithm {

  private static final long serialVersionUID = -7474573037658789063L;

  /** Alpha parameter. Defines how strongly GLSW downweights interferences */
  protected double m_Alpha = 1e-3;

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
      getLogger().warning("Alpha must be > 0 but was " + alpha + ".");
    }
    else {
      m_Alpha = alpha;
      reset();
    }
  }

  @Override
  protected void doReset() {
    m_G = null;
  }

  @Override
  public String toString() {
    ensureConfigured();

    return "Generalized Least Squares Weighting. Projection Matrix shape: " + m_G.shapeString();
  }

  /**
   * Checks the dimensions of the feature and target matrices
   * are valid for this algorithm.
   *
   * @param X                           The feature matrix to check.
   * @param y                           The target matrix to check.
   * @throws MatrixAlgorithmsException  If the check fails.
   */
  protected void check(Matrix X, Matrix y) throws MatrixAlgorithmsException {
    if (X.numRows() != y.numRows() || X.numColumns() != y.numColumns())
      throw new MatrixAlgorithmsException("Matrices X and y must have the same shape");
  }

  @Override
  protected void doConfigure(Matrix X, Matrix y) {
    check(X, y);

    Matrix C = getCovarianceMatrix(X, y);

    // SVD
    Matrix V = getEigenvectorMatrix(C);
    Matrix D = getWeightMatrix(C);

    // Projection Matrix
    m_G = V.mul(D.inverse()).mul(V.t());
  }

  protected Matrix getEigenvectorMatrix(Matrix C) {
    return C.getEigenvalueDecompositionV();
  }

  protected Matrix getWeightMatrix(Matrix C) {
    // Get eigenvalues
    Matrix Ssquared = C.svdS().powElementwise(2);

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
    Matrix x1Centered = c1.configureAndTransform(x1);
    Matrix x2Centered = c2.configureAndTransform(x2);

    // Build difference
    Matrix Xd = x2Centered.sub(x1Centered);

    // Covariance Matrix
    return Xd.t().mul(Xd);
  }

  @Override
  protected Matrix doTransform(Matrix predictors) {
    return predictors.mul(m_G);
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }
}
