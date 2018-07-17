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

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.transformation.Standardize;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * Sparse PLS algorithm.
 * <br>
 * See here:
 * <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2810828/">Sparse partial least squares regression for simultaneous dimension reduction and variable selection</a>
 *
 * Implementation was oriented at the R SPLS package, which implemnets the above
 * mentioned paper:
 * <a href="https://github.com/cran/spls">Sparse Partial Least Squares (SPLS) Regression and Classification</a>
 *
 * @author Steven Lang
 */
public class SparsePLS
  extends AbstractSingleResponsePLS {


  private static final long serialVersionUID = -6097279189841762321L;

  protected Matrix m_Bpls;

  /** NIPALS tolerance threshold */
  protected double m_Tol;

  /** NIPALS max iterations */
  protected int m_MaxIter;

  /** Sparsity parameter. Determines sparseness. */
  protected double m_lambda;

  protected Set<Integer> m_A;

  /** Loadings. */
  protected Matrix m_W;

  /** Standardize X */
  protected Standardize m_StandardizeX;

  /** Standardize Y */
  protected Standardize m_StandardizeY;

  public double getTol() {
    return m_Tol;
  }

  public void setTol(double tol) {
    m_Tol = tol;
  }

  public int getMaxIter() {
    return m_MaxIter;
  }

  public void setMaxIter(int maxIter) {
    m_MaxIter = maxIter;
  }

  public double getLambda() {
    return m_lambda;
  }

  public void setLambda(double lambda) {
    m_lambda = lambda;
  }

  /**
   * Resets the member variables.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Bpls = null;
    m_A = null;
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
  }

  @Override
  protected void initialize() {
    super.initialize();
    m_lambda = 0.5;
    m_Tol = 1e-7;
    m_MaxIter = 500;
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
  }

  /**
   * Returns the all the available matrices.
   *
   * @return the names of the matrices
   */
  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "W",
      "B"
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
      case "W":
	return m_W;
      case "B":
        return m_Bpls;
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
    return getMatrix("W");
  }

  /**
   * Initializes using the provided data.
   *
   * @param predictors the input data
   * @param response   the dependent variable(s)
   * @return null if successful, otherwise error message
   */
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix X, y, wk;
    getLogger();

    X = m_StandardizeX.transform(predictors);
    y = m_StandardizeY.transform(response);
    Matrix Xj = X.copy();
    Matrix yj = y.copy();
    m_A = new TreeSet<>();
    m_Bpls = MatrixFactory.zeros(X.numColumns(), y.numColumns());
    m_W = MatrixFactory.zeros(X.numColumns(), getNumComponents());

    for (int k = 0; k < getNumComponents(); k++) {
      wk = getDirectionVector(Xj, yj, k);
      m_W.setColumn(k, wk);

      if (m_Debug) {
	checkDirectionVector(wk);
      }

      collectIndices(wk);

      Matrix X_A = getColumnSubmatrixOf(X);
      m_Bpls = MatrixFactory.zeros(X.numColumns(), y.numColumns());
      Matrix Bpls_A = getRegressionCoefficient(X_A, y, k);

      // Fill m_Bpls values at non zero indices with estimated
      // regression coefficients
      int idxCounter = 0;
      for (Integer idx : m_A) {
	m_Bpls.setRow(idx, Bpls_A.getRow(idxCounter++));
      }

      // Deflate
      yj = y.sub(X.mul(m_Bpls));
    }

    if (m_Debug) {
      m_Logger.info("Selected following features " +
	"(" + m_A.size() + "/" + X.numColumns() + "): ");
      List<String> l = m_A.stream().map(String::valueOf).collect(Collectors.toList());
      m_Logger.info(String.join(",", l));
    }

    return null;
  }

  /**
   * Calculate NIPALS regression coefficients.
   *
   * @param X_A Predictors subset
   * @param y   Current response vector
   * @param k   PLS iteration
   * @return Bpls (NIPALS regression coefficients)
   * @throws Exception Exception during NIPALS initialization
   */
  private Matrix getRegressionCoefficient(Matrix X_A, Matrix y, int k) throws Exception {
    int numComponents = Math.min(X_A.numColumns(), k + 1);
    NIPALS nipals = new NIPALS();
    nipals.setMaxIter(m_MaxIter);
    nipals.setTol(m_Tol);
    nipals.setNumComponents(numComponents);
    nipals.initialize(X_A, y);
    return nipals.getCoef();
  }

  /**
   * Get the column submatrix of X given by the indices in m_A
   *
   * @param X Input Matrix
   * @return Submatrix of x
   */
  private Matrix getColumnSubmatrixOf(Matrix X) {
    Matrix X_A = MatrixFactory.zeros(X.numRows(), m_A.size());
    int colCount = 0;
    for (Integer i : m_A) {
      Matrix col = X.getColumn(i);
      X_A.setColumn(colCount, col);
      colCount++;
    }
    return X_A;
  }

  /**
   * Get the row submatrix of X given by the indices in m_A
   *
   * @param X Input Matrix
   * @return Submatrix of x
   */
  private Matrix getRowSubmatrixOf(Matrix X) {
    Matrix X_A = MatrixFactory.zeros(m_A.size(), X.numColumns());
    int rowCount = 0;
    for (Integer i : m_A) {
      Matrix row = X.getRow(i);
      X_A.setRow(rowCount, row);
      rowCount++;
    }
    return X_A;
  }

  /**
   * Collect indices based on the current non zero indices in w and m_Bpls
   *
   * @param w Direction Vector
   */
  private void collectIndices(Matrix w) {
    m_A.clear();
    m_A.addAll(w.whereVector(d -> Math.abs(d) > 1e-6));
    m_A.addAll(m_Bpls.whereVector(d -> Math.abs(d) > 1e-6));
  }

  /**
   * Check if the direction vector is fulfills w^Tw=1
   *
   * @param w Direction vector
   */
  private void checkDirectionVector(Matrix w) {
    // Test if w^Tw = 1
    if (w.norm2squared() - 1 > 1e-6) {
      m_Logger.warning("Direction vector condition w'w=1 was violated.");
    }
  }

  /**
   * Compute the direction vector.
   *
   * @param X  Predictors
   * @param yj Current deflated response
   * @param k  Iteration
   * @return Direction vector
   */
  private Matrix getDirectionVector(Matrix X, Matrix yj, int k) {
    Matrix Zp = X.t().mul(yj);
    //    Zp.divi(Zp.norm2()); // Reference paper uses l2 norm
    double znorm = Zp.abs().median(); // R package spls uses median norm
    Zp.divi(znorm);
    Matrix ZpSign = Zp.sign();
    Matrix valb = Zp.abs().sub(m_lambda * Zp.abs().max());

    // Collect indices where valb is >= 0
    List<Integer> idxs = valb.whereVector(d -> d >= 0);
    Matrix preMul = valb.mulElementwise(ZpSign);
    Matrix c = MatrixFactory.zeros(Zp.numRows(), 1);
    for (Integer idx : idxs) {
      double val = preMul.get(idx, 0);
      c.set(idx, 0, val);
    }

    return c.div(c.norm2squared()); // Rescale c and use as estimated direction vector

    /* Extension for multivariate Y (needs further testing):
      Matrix w;
      Matrix c;
      Matrix wOld;
      Matrix M;
      Matrix U;
      Matrix V;
      Matrix cOld;
      double iterationChangeW = m_Tol * 10;
      double iterationChangeC = m_Tol * 10;
      int iterations = 0;

      // Repeat w step and c step until convergence
      while ((iterationChangeW > m_Tol || iterationChangeC > m_Tol) && iterations < m_MaxIter) {

      // w step
      wOld = w;
      M = Xt.mul(yj).mul(yj.t()).mul(X);
      Matrix mtc = M.mul(c);
      U = mtc.svdU();
      V = mtc.svdV();
      w = U.mul(V.t());

      // c step
      cOld = c;
      Zp = Xt.mul(yj);
      Zp.divi(Zp.norm2()); // Reference paper uses l2 norm
      //      double znorm = Zp.abs().median(); // R package spls uses median norm
      //      Zp.divi(znorm);
      Matrix ZpSign = Zp.sign();
      Matrix valb = Zp.abs().sub(m_lambda * Zp.abs().max());

      // Collect indices where valb is >= 0
      List<Integer> idxs = valb.whereVector(d -> d >= 0);
      Matrix preMul = valb.mulElementwise(ZpSign);
      c = new Matrix(Zp.numRows(), 1);
      for (Integer idx : idxs) {
	double val = preMul.get(idx, 0);
	c.set(idx, 0, val);
      }

      // Update stopping conditions
      iterations++;
      iterationChangeW = w.sub(wOld).norm2();
      iterationChangeC = c.sub(cOld).norm2();
    }
    return w;*/
  }

  /**
   * Transforms the data.
   *
   * @param predictors the input data
   * @return the transformed data and the predictions
   */
  @Override
  protected Matrix doTransform(Matrix predictors) {
    int numComponents = getNumComponents();
    Matrix T = MatrixFactory.zeros(predictors.numRows(), numComponents);
    Matrix X = predictors.copy();
    for (int k = 0; k < numComponents; k++) {
      Matrix wk = m_W.getColumn(k);
      Matrix tk = X.mul(wk);
      T.setColumn(k, tk);

      Matrix pk = X.t().mul(tk);
      X.subi(tk.mul(pk.t()));
    }

    return T;
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
   */
  @Override
  protected Matrix doPerformPredictions(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    Matrix X_A = getColumnSubmatrixOf(X);
    Matrix B_A = getRowSubmatrixOf(m_Bpls);

    Matrix yMeans = MatrixFactory.fromColumn(m_StandardizeY.getMeans());
    Matrix yStd = MatrixFactory.fromColumn(m_StandardizeY.getStdDevs());
    Matrix yhat = X_A.mul(B_A).scaleByVector(yStd).addByVector(yMeans);

    return yhat;
  }
}
