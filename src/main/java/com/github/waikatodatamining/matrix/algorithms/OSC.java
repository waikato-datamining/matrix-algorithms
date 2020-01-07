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
 * OSC.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.algorithm.SupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;

/**
 * Implementation of the Orthogonal Signal Correction algorithm.
 *
 * See here:
 * <a href="https://www.sciencedirect.com/science/article/pii/S0169743998001099">Orthogonal signal correction of near-infrared spectra</a>
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class OSC
  extends SupervisedMatrixAlgorithm {

  /** The number of OSC components to extract from the matrix. */
  protected int m_NumComponents = 1;

  /** The "W" weights matrix. */
  protected Matrix m_W;

  /** The "P" loadings matrix. */
  protected Matrix m_P;

  public int getNumComponents() {
    return m_NumComponents;
  }

  public void setNumComponents(int value) {
    if (value < 1)
      throw new RuntimeException("Number of OSC components must be at least 1, got " + value);

    m_NumComponents = value;
    reset();
  }

  @Override
  protected void doConfigure(Matrix X, Matrix y) {
    // Initialise the weights and loading matrices
    m_W = MatrixFactory.zeros(X.numColumns(), m_NumComponents);
    m_P = MatrixFactory.zeros(m_NumComponents, X.numColumns());

    // (1) Optionally transform, center, and scale the data to give the `raw' matrices X and Y
    // (This stage should be handled externally if required)

    for (int oscComponent = 0; oscComponent < m_NumComponents; oscComponent++) {
      // (2) Start by calculating the first principal component of X, with the score vector, t
      Matrix t = firstPrincipalComponent(X);

      Matrix t_new;
      Matrix w;
      do {
        // (3) Orthogonalize t to Y
        t_new = orthogonalise(t, y);

        // (4) Calculate a weight vector, w, that makes Xw=t
        Matrix X_inverse = generalisedInverseViaPLSEstimation(X);
        w = X_inverse.mul(t_new);

        // (5) Calculate a new score vector from X and w
        t = X.mul(w);

        // (6) Check for convergence, by testing if t has stabilized
      } while (!convergence(t_new, t));

      // (7) Compute a loading vector, p
      Matrix p = t.transpose().mul(X).div(t.norm2squared());

      // Update the weights and loading matrices
      m_W.setColumn(oscComponent, w);
      m_P.setRow(oscComponent, p);

      // (8) Subtract the `correction' from X, to give residuals, E
      // (9) Continue with the next `component' using E as X, then another one, etc., until satisfaction
      X = X.sub(t.mul(p));
    }
  }

  /**
   * Calculates the inverse of the given matrix.
   *
   * TODO: The reference paper doesn't give implementation details
   *       on how to do this, so we just use the inverse method. If
   *       further details come to light, apply these instead (if
   *       relevant).
   *
   * @param X   The matrix to invert.
   * @return    The inverse of the given matrix.
   */
  public static Matrix generalisedInverseViaPLSEstimation(Matrix X) {
    return X.inverse();
  }

  /**
   * Determines if score convergence has been reached by comparing
   * subsequent values of the score vector.
   *
   * @param t       The current score vector.
   * @param t_old   The last score vector (before this iteration).
   * @return        Whether convergence has been reached.
   */
  public static boolean convergence(Matrix t, Matrix t_old) {
    return t.sub(t_old).norm2() / t.norm2() < 1e-6;
  }

  /**
   * Calculates the first principal component of a matrix.
   *
   * @param X   The matrix.
   * @return    The first principal component.
   */
  public static Matrix firstPrincipalComponent(Matrix X) {
    // Get the covariance matrix of the given matrix
    Matrix covarianceX = MatrixHelper.covariance(X);

    // Get the dominant eigenvector of the covariance matrix
    Matrix dominantEigenvector = covarianceX.getDominantEigenvector();

    return X.mul(dominantEigenvector);
  }

  /**
   * Orthogonalises a matrix against another matrix.
   *
   * @param t         The matrix to orthogonalise.
   * @param against   The matrix that t should be orthogonal to.
   * @return          The orthogonalised matrix.
   */
  public static Matrix orthogonalise(Matrix t, Matrix against) {
    // Cache the transpose of the "against" matrix
    Matrix againstTranspose = against.transpose();

    // Calculate the orthogonalisation matrix
    Matrix orthogonalisationMatrix = againstTranspose.mul(against).inverse();
    orthogonalisationMatrix = against.mul(orthogonalisationMatrix).mul(againstTranspose);

    return t.sub(orthogonalisationMatrix.mul(t));
  }

  @Override
  protected void doReset() {
    m_W = null;
    m_P = null;
  }

  @Override
  protected Matrix doTransform(Matrix X) {
    // Remove the orthogonal components
    for (int oscComponent = 0; oscComponent < m_NumComponents; oscComponent++) {
      // Get the weight and loading for this component
      Matrix w = m_W.getColumn(oscComponent);
      Matrix p = m_P.getRow(oscComponent);

      // Calculate the scores
      Matrix t = X.mul(w);

      // Remove the orthogonal aspect of this component
      X = X.sub(t.mul(p));
    }

    return X;
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }
}
