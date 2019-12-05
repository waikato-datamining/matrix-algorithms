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
 * SupervisedMatrixAlgorithmWithResponseTransform.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.algorithm;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Base-class for algorithms that can transform target matrices as
 * well as feature matrices.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public abstract class SupervisedMatrixAlgorithmWithResponseTransform
  extends SupervisedMatrixAlgorithm {

  /**
   * Transforms the target matrix, configuring the algorithm on the matrices
   * if it is not already configured.
   *
   * @param X   The feature matrix to configure on.
   * @param y   The target matrix to configure on and apply the algorithm to.
   * @return    The matrix resulting from the transformation.
   */
  public final Matrix configureAndTransformResponse(Matrix X, Matrix y) {
    if (!isConfigured())
      configure(X, y);

    return transformResponse(y);
  }

  /**
   * Performs the transformation that this algorithm represents on
   * the given target matrix.
   *
   * @param y   The target matrix to apply the algorithm to.
   * @return    The matrix resulting from the transformation.
   */
  public final Matrix transformResponse(Matrix y) {
    // Ensure the algorithm is configured
    ensureConfigured();

    return doTransformResponse(y);
  }

  /**
   * Internal implementation of algorithm transformation. Override
   * to implement the transformation-specific code.
   *
   * @param y   The target matrix to apply the algorithm to.
   * @return    The matrix resulting from the transformation.
   */
  protected abstract Matrix doTransformResponse(Matrix y);

}
