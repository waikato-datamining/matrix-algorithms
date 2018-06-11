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
 * AbstractTransformation.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.LoggingObject;

/**
 * Ancestor for matrix transformations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractTransformation
  extends LoggingObject {

  private static final long serialVersionUID = 4728192847051793396L;

  /** whether the transformer has been configured. */
  protected boolean m_Configured;

  /**
   * For resetting data structures when changing parameters.
   */
  protected void reset() {
    m_Configured = false;
  }

  /**
   * Configures the transformer.
   *
   * @param data	the data to configure with
   */
  public abstract void configure(Matrix data);

  /**
   * Transforms the data.
   *
   * @param data	the data to transform
   * @return		the transformed data
   */
  protected abstract Matrix doTransform(Matrix data);

  /**
   * Transforms the data.
   *
   * @param data	the data to transform
   * @return		the transformed data
   */
  public Matrix transform(Matrix data) {
    if (!m_Configured)
      configure(data);
    return doTransform(data);
  }

  /**
   * Inverse transforms the data.
   *
   * @param data	the data to reverse transform
   * @return		the inverse transformed data
   */
  protected abstract Matrix doInverseTransform(Matrix data);

  /**
   * Inverse transforms the data.
   *
   * @param data	the data to reverse transform
   * @return		the inverse transformed data
   */
  public Matrix inverseTransform(Matrix data) {
    if (!m_Configured)
      configure(data);
    return doInverseTransform(data);
  }
}
