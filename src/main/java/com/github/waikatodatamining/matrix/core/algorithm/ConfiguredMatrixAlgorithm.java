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
 * ConfiguredMatrixAlgorithm.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.algorithm;

import com.github.waikatodatamining.matrix.core.exceptions.UnconfiguredAlgorithmException;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Base class for algorithms that require configuration matrices
 * before they can perform their transformation. Package-private
 * as algorithms themselves should not sub-class this class, but
 * instead sub-classes of this class which expose the configuration
 * method.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
abstract class ConfiguredMatrixAlgorithm
  extends MatrixAlgorithm {

  /** Whether the algorithm has been configured. */
  private boolean m_Configured;

  protected ConfiguredMatrixAlgorithm() {
    m_Configured = false;
  }

  /**
   * Resets the algorithm to its unconfigured state.
   */
  public final void reset() {
    doReset();
    m_Configured = false;
  }

  /**
   * Resets the algorithm to its unconfigured state. Override
   * to reset configured algorithm state.
   */
  protected abstract void doReset();

  /**
   * Allows sub-classes to set the configured flag
   * once they have been configured. Package-private
   * so that algorithms themselves don't set this, but
   * the specific configuration sub-types do on their
   * behalf.
   */
  final void setConfigured() {
    m_Configured = true;
  }

  /**
   * Whether this algorithm has been configured.
   *
   * @return  True if the algorithm is configured,
   *          false if not.
   */
  public final boolean isConfigured() {
    return m_Configured;
  }

  /**
   * Throws {@link UnconfiguredAlgorithmException} if this
   * algorithm hasn't been configured yet.
   *
   * @throws UnconfiguredAlgorithmException   If the algorithm
   *                                          is not configured.
   */
  public final void ensureConfigured() throws UnconfiguredAlgorithmException {
    if (!isConfigured())
      throw new UnconfiguredAlgorithmException(getClass());
  }

  @Override
  public Matrix transform(Matrix X) {
    // Ensure the algorithm is configured
    ensureConfigured();

    return super.transform(X);
  }

  @Override
  public Matrix inverseTransform(Matrix X) {
    // Ensure the algorithm is configured
    ensureConfigured();

    return super.inverseTransform(X);
  }

  @Override
  public boolean isNonInvertible() {
    // Non-configured algorithms are never invertible
    return !isConfigured();
  }
}
