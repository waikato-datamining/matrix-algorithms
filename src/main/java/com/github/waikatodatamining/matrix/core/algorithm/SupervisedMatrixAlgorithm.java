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
 * SupervisedMatrixAlgorithm.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.algorithm;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Base class for algorithms that are configured on a feature
 * and target matrix.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public abstract class SupervisedMatrixAlgorithm
  extends ConfiguredMatrixAlgorithm {

  /**
   * Transforms the feature matrix, configuring the algorithm on the matrices
   * if it is not already configured.
   *
   * @param X   The feature matrix to configure on and apply the algorithm to.
   * @param y   The target matrix to configure on.
   * @return    The matrix resulting from the transformation.
   */
  public final Matrix configureAndTransform(Matrix X, Matrix y) {
    // Configure on first pair of matrices seen if not done explicitly
    if (!isConfigured())
      configure(X, y);

    return transform(X);
  }

  /**
   * Configures this algorithm on the given feature and target matrices.
   *
   * @param X   The feature configuration matrix.
   * @param y   The target configuration matrix.
   */
  public final void configure(Matrix X, Matrix y) {
    // Check that a configuration matrix was given
    if (X == null)
      throw new NullPointerException("Cannot configure on null feature matrix");
    else if (y == null)
      throw new NullPointerException("Cannot configure on null target matrix");

    // Perform actual configuration
    doConfigure(X, y);

    // Flag that we are configured
    setConfigured();
  }

  /**
   * Configuration-specific implementation. Override to configure
   * the algorithm on the given matrices.
   *
   * @param X   The feature configuration matrix.
   * @param y   The target configuration matrix.
   */
  protected abstract void doConfigure(Matrix X, Matrix y);

}
