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

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.StoppedException;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.algorithm.MatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import com.github.waikatodatamining.matrix.core.Utils;

/**
 * Normalises the data in each row of a matrix.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class RowNorm
  extends MatrixAlgorithm {

  private static final long serialVersionUID = -4619086306634317821L;

  @Override
  protected Matrix doTransform(Matrix data) {
    // Calculate the mean and standard deviation for each row
    double[] means = new double[data.numRows()];
    double[] stdDevs = new double[data.numRows()];
    for (int rowIndex = 0; rowIndex < data.numRows(); rowIndex++) {
      means[rowIndex] = MatrixHelper.mean(data, rowIndex, false);
      stdDevs[rowIndex] = MatrixHelper.stdev(data, rowIndex, false);
    }

    // Debug: Log the means and standard deviations
    if (getDebug()) {
      getLogger().info("Means: " + Utils.arrayToString(means));
      getLogger().info("StdDevs: " + Utils.arrayToString(stdDevs));
    }

    // Create a result matrix
    Matrix result = data.copy();

    // Normalise each row
    for (int rowIndex = 0; rowIndex < result.numRows(); rowIndex++) {
      if (m_Stopped)
	throw new StoppedException();

      // Get the mean and standard deviation for this row
      double mean = means[rowIndex];
      double stdDev = stdDevs[rowIndex];

      // Normalise each entry in the row
      for (int columnIndex = 0; columnIndex < result.numColumns(); columnIndex++) {
        result.set(rowIndex, columnIndex,
              Utils.normalise(result.get(rowIndex, columnIndex), mean, stdDev));
      }
    }

    return result;
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }
}
