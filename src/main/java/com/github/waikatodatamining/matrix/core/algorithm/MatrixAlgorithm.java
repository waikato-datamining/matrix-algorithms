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
 * MatrixAlgorithm.java
 * Copyright (C) 2019-2024 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.algorithm;

import com.github.waikatodatamining.matrix.core.LoggingObject;
import com.github.waikatodatamining.matrix.core.exceptions.InverseTransformException;
import com.github.waikatodatamining.matrix.core.exceptions.UninvertibleAlgorithmException;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;

/**
 * Base class for all matrix algorithms.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public abstract class MatrixAlgorithm
  extends LoggingObject {

  /** whether the transform was stopped. */
  protected boolean m_Stopped;

  /**
   * Performs the transformation that this algorithm represents on
   * the given matrix.
   *
   * @param X   The matrix to apply the algorithm to.
   * @return    The matrix resulting from the transformation.
   */
  public Matrix transform(Matrix X) {
    if (X == null)
      throw new NullPointerException("Can't transform null matrix");

    return doTransform(X);
  }

  /**
   * Internal implementation of algorithm transformation. Override
   * to implement the transformation-specific code.
   *
   * @param X   The matrix to apply the algorithm to.
   * @return    The matrix resulting from the transformation.
   */
  protected abstract Matrix doTransform(Matrix X);

  /**
   * Performs the inverse of the transformation that this algorithm
   * represents on the given matrix.
   *
   * @param X   The matrix to inverse-apply the algorithm to.
   * @return    The matrix resulting from the inverse-transformation.
   */
  public Matrix inverseTransform(Matrix X) throws InverseTransformException {
    if (X == null)
      throw new NullPointerException("Can't inverse-transform null matrix");

    return doInverseTransform(X);
  }

  /**
   * Internal implementation of algorithm inverse-transformation. Override
   * to implement the transformation-specific code.
   *
   * @param X   The matrix to inverse-apply the algorithm to.
   * @return    The matrix resulting from the inverse-transformation.
   */
  protected Matrix doInverseTransform(Matrix X) throws InverseTransformException {
    throw new UninvertibleAlgorithmException(getClass());
  }

  /**
   * Whether the algorithm is currently non-invertible. If it's
   * not certain whether an inversion will fail (e.g. it depends
   * on the input), this method should return false, and calling
   * {@link MatrixAlgorithm#inverseTransform(Matrix)} will throw
   * {@link InverseTransformException} if it does fail. Meant to
   * provide a shortcut where performing the inverse-transform
   * may be expensive if it is possible but it is also possible to
   * tell in advance in some cases that it is not possible. Also
   * used for algorithms that are always impossible to invert.
   *
   * @return  Whether the algorithm is currently definitely impossible
   *          to invert.
   */
  public boolean isNonInvertible() {
    return false;
  }

  /**
   * Sets the stopped flag, indicating the algorithm to throw an exception.
   */
  public void stop() {
    m_Stopped = true;
  }

  /**
   * Returns whether the algorithm was stopped.
   *
   * @return		true if stopped
   */
  public boolean isStopped() {
    return m_Stopped;
  }
}
