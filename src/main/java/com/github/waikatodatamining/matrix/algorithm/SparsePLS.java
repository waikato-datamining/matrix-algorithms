/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PLS1.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import java.util.Set;
import java.util.TreeSet;

import static com.github.waikatodatamining.matrix.core.MatrixHelper.l2VectorNorm;

/**
 * Sparse PLS algorithm.
 * <br>
 * See here:
 * <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2810828/">Sparse partial least squares regression for simultaneous dimension reduction and variable selection</a>
 *
 * @author Steven Lang
 */
public class SparsePLS
  extends AbstractSingleResponsePLS {


  private static final long serialVersionUID = -6097279189841762321L;

  protected Matrix m_Bpls;

  protected double m_Tol;

  protected int m_MaxIter;

  protected double m_lambda;

  protected Set<Integer> m_A;

  /**
   * Resets the member variables.
   */
  @Override
  protected void reset() {
    super.reset();
  }

  @Override
  protected void initialize() {
    super.initialize();
  }

  /**
   * Returns the all the available matrices.
   *
   * @return the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
    };
  }

  /**
   * Returns the matrix with the specified name.
   *
   * @param name the name of the matrix
   * @return the matrix, null if not available
   */
  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      default:
	return null;
    }
  }

  /**
   * Whether the algorithm supports return of loadings.
   *
   * @return true if supported
   * @see #getLoadings()
   */
  public boolean hasLoadings() {
    return true;
  }

  /**
   * Returns the loadings, if available.
   *
   * @return the loadings, null if not available
   */
  public Matrix getLoadings() {
    return getMatrix("");
  }

  /**
   * Initializes using the provided data.
   *
   * @param predictors the input data
   * @param response   the dependent variable(s)
   * @return null if successful, otherwise error message
   */
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix X, Xtrans, M, U, V, Zp, y, w, wOld, c, cOld;

    X = predictors.copy();
    y = response;
    m_A = new TreeSet<>();

    Matrix yj = y.copy();

    for (int k = 0; k < getNumComponents(); k++) {
      w = getDirectionVector(X, y, yj, k);
      checkDirectionVector(w);
      collectIndices(w);
      Matrix X_A = getSubMatrixOfX(X);


      NIPALS pls = new NIPALS();
      pls.setNumCoefficients(k);
      pls.initialize(X_A, yj); // fit on yj or y?

      // Todo: Update m_Bpls with PLS estimates of the direction vectors

      yj = y.minus(X.times(m_Bpls));
    }

    return null;
  }

  /**
   * Get the submatrix of X given by the indices in m_A
   *
   * @param x Input Matrix
   * @return Submatrix of x
   */
  private Matrix getSubMatrixOfX(Matrix x) {
    Matrix X_A = new Matrix(x.getRowDimension(), m_A.size());
    int colCount = 0;
    for (Integer i : m_A) {
      Matrix col = MatrixHelper.getVector(x, i);
      MatrixHelper.setColumnVector(col, X_A, colCount);
      colCount++;
    }
    return X_A;
  }

  /**
   * Collect indices based on the current non zero indices in w and m_Bpls
   *
   * @param w Direction Vector
   */
  private void collectIndices(Matrix w) {
    // Collect indices for X_A
    for (int i = 0; i < w.getRowDimension(); i++) {
      if (w.get(i, 0) > 1e-6) {
	m_A.add(i);
      }
      if (m_Bpls.get(i, 0) > 1e-6) {
	m_A.add(i);
      }
    }
  }

  /**
   * Check if the direction vector is fulfills w^Tw=1
   *
   * @param w Direction vector
   */
  private void checkDirectionVector(Matrix w) {
    // Test if w^Tw = 1
    if (Math.abs(w.transpose().times(w).get(0, 0) - 1) > 1e-6) {
      m_Logger.warning("Something is off");
    }
  }

  /**
   * Compute the direction vector.
   *
   * @param x  Predictors
   * @param y  Response
   * @param yj Current deflated response
   * @param k  Iteration
   * @return Direction vector
   */
  private Matrix getDirectionVector(Matrix x, Matrix y, Matrix yj, int k) {
    Matrix w;
    Matrix c;
    Matrix wOld;
    Matrix M;
    Matrix U;
    Matrix V;
    Matrix cOld;
    Matrix Zp;
    Matrix xtrans = x.transpose();
    double iterationChangeW = m_Tol * 10;
    double iterationChangeC = m_Tol * 10;
    int iterations = 0;

    w = MatrixHelper.getVector(y, 0);
    c = MatrixHelper.randn(w.getRowDimension(), w.getColumnDimension(), k);
    MatrixHelper.normalizeVector(c);

    // Repeat w step and c step until convergence
    while ((iterationChangeW > m_Tol || iterationChangeC > m_Tol) && iterations < m_MaxIter) {

      // w step
      wOld = w;
      M = xtrans.times(yj).times(yj.transpose()).times(x);
      SingularValueDecomposition svd = M.times(c).svd();
      U = svd.getU();
      V = svd.getV();
      w = U.times(V.transpose());

      // c step
      cOld = c;
      Zp = xtrans.times(yj).times(1.0 / xtrans.times(yj).norm2());
      double max = StrictMath.max(Zp.norm2() - m_lambda / 2.0, 0.0);
      MatrixHelper.sign(Zp);
      c = Zp.times(max);
      MatrixHelper.normalizeVector(c);

      // Update stopping conditions
      iterations++;
      iterationChangeW = l2VectorNorm(w.minus(wOld));
      iterationChangeC = l2VectorNorm(c.minus(cOld));
    }
    return w;
  }

  /**
   * Get the inverse of the squared l2 norm.
   *
   * @param v Input vector
   * @return 1.0 / norm2(v)^2
   */
  protected double invL2Squared(Matrix v) {
    double l2 = l2VectorNorm(v);
    return 1.0 / (l2 * l2);
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   * @throws Exception if analysis fails
   */
  @Override
  protected Matrix doTransform(Matrix predictors) {

  }

  /**
   * Returns whether the algorithm can make predictions.
   *
   * @return true if can make predictions
   */
  public boolean canPredict() {
    return true;
  }

  /**
   * Performs predictions on the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   * @throws Exception if analysis fails
   */
  @Override
  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {

  }
}