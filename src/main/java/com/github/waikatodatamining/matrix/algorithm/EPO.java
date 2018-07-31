package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;

/**
 * External Parameter Orthogonalization (EPO)
 * <p>
 * EPO is based on GLSW with the change, that the D matrix is the identity
 * matrix and only a certain number of eigenvectors are kept after applying SVD.
 * <p>
 * See also: <a href="http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#External_Parameter_Orthogonalization_.28EPO.29">External Parameter Orthogonalization (EPO)</a>
 *
 * @author Steven Lang
 */
public class EPO extends GLSW {

  private static final long serialVersionUID = -4961123476766554940L;

  /** Number of eigenvectors to keep. */
  protected int m_N;

  public double getN() {
    return m_N;
  }

  public void setN(int n) {
    if (n <= 0) {
      m_Logger.warning("Number of eigenvectors to keep must be > 0 but was " + n + ".");
    }
    else {
      m_N = n;
      reset();
    }
  }

  @Override
  protected void initialize() {
    super.initialize();
    m_N = 5;
  }

  /**
   * Instead of calculating D from C, create an identity matrix.
   *
   * @param C Covariance matrix
   * @return Identity matrix
   */
  @Override
  protected Matrix getWeightMatrix(Matrix C) {
    return MatrixFactory.eye(m_N);
  }

  /**
   * Only return the first {@code N} eigenvectors.
   *
   * @param C Covariance matrix
   * @return Matrix with first {@code N} eigenvectors
   */
  @Override
  protected Matrix getEigenvectorMatrix(Matrix C) {
    boolean sortDominance = true;
    Matrix V = C.getEigenvectors(sortDominance);
    V = V.getSubMatrix(0, V.numRows(), 0, Math.min(V.numColumns(), m_N));
    return V;
  }
}
