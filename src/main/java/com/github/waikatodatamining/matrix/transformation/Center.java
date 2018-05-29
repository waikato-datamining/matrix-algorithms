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
 * Center.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Centers the data in the matrix columns according to the mean.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Center
  extends AbstractTransformation {

  private static final long serialVersionUID = -1411842792256545682L;

  /** the means. */
  protected double[] m_Means;

  /**
   * Resets the transformer.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Means = null;
  }

  /**
   * Configures the transformer.
   *
   * @param data	the data to configure with
   */
  @Override
  public void configure(Matrix data) {
    int		j;

    m_Means = new double[data.getColumnDimension()];
    for (j = 0; j < data.getColumnDimension(); j++)
      m_Means[j] = MatrixHelper.mean(data, j);

    if (getDebug())
      getLogger().info("Means: " + Utils.arrayToString(m_Means));

    m_Configured = true;
  }

  /**
   * Transforms the data.
   *
   * @param data	the data to transform
   * @return		the transformed data
   */
  @Override
  protected Matrix doTransform(Matrix data) {
    Matrix	result;
    int		i;
    int		j;

    result = data.copy();
    for (j = 0; j < result.getColumnDimension(); j++) {
      if (m_Means[j] != 0) {
	for (i = 0; i < result.getRowDimension(); i++) {
	  result.set(i, j, result.get(i, j) - m_Means[j]);
	}
      }
    }

    return result;
  }

  @Override
  protected Matrix doInverseTransform(Matrix data) {
    Matrix result;
    int i;
    int j;

    result = data.copy();
    for (j = 0; j < result.getColumnDimension(); j++) {
      if (m_Means[j] != 0) {
        for (i = 0; i < result.getRowDimension(); i++) {
          result.set(i, j, result.get(i, j) + m_Means[j]);
        }
      }
    }

    return result;
  }
}
