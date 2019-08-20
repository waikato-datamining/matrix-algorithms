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
 * RowNorm.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Normalises the data in each row of a matrix.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class RowNorm extends AbstractTransformation {

  private static final long serialVersionUID = -4619086306634317821L;

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
    int		j;

    m_Means = new double[data.numRows()];
    for (j = 0; j < data.numRows(); j++)
      m_Means[j] = MatrixHelper.mean(data, j, false);

    m_StdDevs = new double[data.numRows()];
    for (j = 0; j < data.numRows(); j++)
      m_StdDevs[j] = MatrixHelper.stdev(data, j, false);

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
    int		columnIndex;
    int		rowIndex;

    result = data.copy();
    for (rowIndex = 0; rowIndex < result.numRows(); rowIndex++) {
      if (m_StdDevs[rowIndex] > 0) {
	for (columnIndex = 0; columnIndex < result.numColumns(); columnIndex++) {
	  result.set(rowIndex, columnIndex, (result.get(rowIndex, columnIndex) - m_Means[rowIndex]) / m_StdDevs[rowIndex]);
	}
      }
      else if (m_Means[rowIndex] != 0) {
	for (columnIndex = 0; columnIndex < result.numColumns(); columnIndex++) {
	  result.set(rowIndex, columnIndex, result.get(rowIndex, columnIndex) - m_Means[rowIndex]);
	}
      }
    }

    return result;
  }

  @Override
  protected Matrix doInverseTransform(Matrix data) {
    Matrix	result;
    int		columnIndex;
    int		rowIndex;

    result = data.copy();
    for (rowIndex = 0; rowIndex < result.numRows(); rowIndex++) {
      if (m_StdDevs[rowIndex] > 0) {
	for (columnIndex = 0; columnIndex < result.numColumns(); columnIndex++) {
	  result.set(rowIndex, columnIndex, (result.get(rowIndex, columnIndex) * m_StdDevs[rowIndex]) + m_Means[rowIndex]);
	}
      } else if (m_Means[rowIndex] != 0) {
	for (columnIndex = 0; columnIndex < result.numColumns(); columnIndex++) {
	  result.set(rowIndex, columnIndex, result.get(rowIndex, columnIndex) + m_Means[rowIndex]);
	}
      }
    }

    return result;
  }

  public double[] getMeans() {
    return m_Means;
  }

  public double[] getStdDevs() {
    return m_StdDevs;
  }
}
