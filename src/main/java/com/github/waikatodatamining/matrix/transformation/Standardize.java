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
 * Standardize.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Standardizes the data in the matrix columns according to the mean and stdev.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Standardize
  extends AbstractTransformation {

  private static final long serialVersionUID = 3277972065292851486L;

  /** the means. */
  protected double[] m_Means;

  /** the stdevs. */
  protected double[] m_StdDevs;

  /**
   * Resets the transformer.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Means   = null;
    m_StdDevs = null;
  }

  /**
   * Configures the transformer.
   *
   * @param data	the data to configure with
   */
  @Override
  public void configure(Matrix data) {
    int		i;
    int		j;

    m_Means   = new double[data.getColumnDimension()];
    for (j = 0; j < data.getColumnDimension(); j++) {
      for (i = 0; i < data.getRowDimension(); i++) {
        m_Means[j] += data.get(i, j) / data.getRowDimension();
      }
    }

    m_StdDevs = new double[data.getColumnDimension()];
    for (j = 0; j < data.getColumnDimension(); j++) {
      for (i = 0; i < data.getRowDimension(); i++) {
        m_StdDevs[j] += Math.pow(data.get(i, j) - m_Means[j], 2);;
      }
      m_StdDevs[j] /= (data.getRowDimension() - 1);
      m_StdDevs[j] = Math.sqrt(m_StdDevs[j]);
    }

    if (getDebug()) {
      getLogger().info("Means: " + Utils.arrayToString(m_Means));
      getLogger().info("StdDevs: " + Utils.arrayToString(m_StdDevs));
    }
  }

  /**
   * Filters the data.
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
      if (m_StdDevs[j] > 0) {
	for (i = 0; i < result.getRowDimension(); i++) {
	  result.set(i, j, (result.get(i, j) - m_Means[j]) / m_StdDevs[j]);
	}
      }
      else if (m_Means[j] != 0) {
	for (i = 0; i < result.getRowDimension(); i++) {
	  result.set(i, j, result.get(i, j) - m_Means[j]);
	}
      }
    }

    return result;
  }
}
