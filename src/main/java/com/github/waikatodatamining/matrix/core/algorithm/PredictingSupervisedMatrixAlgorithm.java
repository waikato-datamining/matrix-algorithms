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
 * PredictingSupervisedMatrixAlgorithm.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.algorithm;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Base class for algorithms that, once configured, can predict
 * target values from feature matrices.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public abstract class PredictingSupervisedMatrixAlgorithm
  extends SupervisedMatrixAlgorithm {

  /**
   * Performs predictions on the feature matrix, configuring the
   * algorithm on the matrices if it is not already configured.
   *
   * @param X   The feature matrix to configure on and predict against.
   * @param y   The target matrix to configure on.
   * @return    The predictions.
   */
  public final Matrix configureAndPredict(Matrix X, Matrix y) {
    // Configure on first pair of matrices seen if not done explicitly
    if (!isConfigured())
      configure(X, y);

    return predict(X);
  }

  /**
   * Performs predictions on the feature matrix.
   *
   * @param X   The feature matrix to predict against.
   * @return    The predictions.
   */
  public final Matrix predict(Matrix X) {
    if (X == null)
      throw new NullPointerException("Can't predict against null feature matrix");

    // Ensure the algorithm is configured
    ensureConfigured();

    return doPredict(X);
  }

  /**
   * Prediction-specific implementation. Override to predict target
   * values for the given feature matrix.
   *
   * @param X   The feature matrix to predict against.
   * @return    The predictions.
   */
  protected abstract Matrix doPredict(Matrix X);

}
