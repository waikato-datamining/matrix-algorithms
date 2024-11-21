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
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.StoppedException;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Centers the data in the matrix columns according to the mean.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class Center
  extends UnsupervisedMatrixAlgorithm {

  /** The column means. */
  protected double[] m_Means;

  @Override
  public void doReset() {
    m_Means = null;
  }

  @Override
  public void doConfigure(Matrix data) {
    int		j;

    m_Means = new double[data.numColumns()];
    for (j = 0; j < data.numColumns(); j++)
      m_Means[j] = MatrixHelper.mean(data, j);

    if (getDebug())
      getLogger().info("Means: " + Utils.arrayToString(m_Means));
  }

  @Override
  protected Matrix doTransform(Matrix X) {
    Matrix	result;
    int		i;
    int		j;

    result = X.copy();
    for (j = 0; j < result.numColumns(); j++) {
      if (m_Stopped)
	throw new StoppedException();

      if (m_Means[j] != 0) {
        for (i = 0; i < result.numRows(); i++) {
          result.set(i, j, Utils.normalise(result.get(i, j) , m_Means[j], 0));
        }
      }
    }

    return result;
  }

  @Override
  public Matrix doInverseTransform(Matrix data) {
    Matrix result;
    int i;
    int j;

    result = data.copy();
    for (j = 0; j < result.numColumns(); j++) {
      if (m_Means[j] != 0) {
        for (i = 0; i < result.numRows(); i++) {
          result.set(i, j, Utils.unnormalise(result.get(i, j) , m_Means[j], 0));
        }
      }
    }

    return result;
  }

}
