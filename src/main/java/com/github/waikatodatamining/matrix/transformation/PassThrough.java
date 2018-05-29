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
 * PassThrough.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import Jama.Matrix;

/**
 * Dummy, does nothing.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PassThrough
  extends AbstractTransformation {

  private static final long serialVersionUID = 8662408709654176200L;

  /**
   * Configures the transformer.
   *
   * @param data	the data to configure with
   */
  @Override
  public void configure(Matrix data) {
    m_Configured = true;
  }

  /**
   * Just returns the input data.
   *
   * @param data	the data to transform
   * @return		the transformed data
   */
  @Override
  protected Matrix doTransform(Matrix data) {
    return data;
  }

  @Override
  protected Matrix doInverseTransform(Matrix data) {
    return data;
  }
}
