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

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Standardizes the data in the matrix columns according to the mean and stdev.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Standardize
  extends UnsupervisedMatrixAlgorithm {

  /** The column means. */
  protected double[] m_Means;

  /** The column standard deviations. */
  protected double[] m_StdDevs;

  @Override
  public void doReset() {
    m_Means   = null;
    m_StdDevs = null;
  }

  @Override
  public void doConfigure(Matrix data) {
    int		j;

    m_Means = new double[data.numColumns()];
    m_StdDevs = new double[data.numColumns()];
    for (j = 0; j < data.numColumns(); j++) {
      m_Means[j] = MatrixHelper.mean(data, j);
      m_StdDevs[j] = MatrixHelper.stdev(data, j);
    }

    if (getDebug()) {
      getLogger().info("Means: " + Utils.arrayToString(m_Means));
      getLogger().info("StdDevs: " + Utils.arrayToString(m_StdDevs));
    }
  }

  @Override
  protected Matrix doTransform(Matrix data) {
    Matrix	result;
    int		i;
    int		j;

    result = data.copy();
    for (j = 0; j < result.numColumns(); j++) {
      for (i = 0; i < result.numRows(); i++) {
        result.set(i, j, Utils.normalise(result.get(i, j), m_Means[j], m_StdDevs[j]));
      }
    }

    return result;
  }

  @Override
  protected Matrix doInverseTransform(Matrix data) {
    Matrix	result;
    int		i;
    int		j;

    result = data.copy();
    for (j = 0; j < result.numColumns(); j++) {
      for (i = 0; i < result.numRows(); i++) {
        result.set(i, j, Utils.unnormalise(result.get(i, j), m_Means[j], m_StdDevs[j]));
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
