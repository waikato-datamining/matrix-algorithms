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
 * Downsample.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.filter;

import com.github.waikatodatamining.matrix.core.api.Filter;
import com.github.waikatodatamining.matrix.core.LoggingObject;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;

/**
 * Filter which gets every Nth row from a matrix, starting at a
 * given index.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class Downsample
  extends LoggingObject
  implements Filter {

  /** The index to start sampling from. **/
  protected int m_StartIndex = 0;

  /** The step-size between samples. */
  protected int m_Step = 1;

  /**
   * Gets the start index.
   */
  public int getStartIndex() {
    return m_StartIndex;
  }

  /**
   * Sets the start index. Must be at least 0.
   */
  public void setStartIndex(int value) {
    if (value < 0) {
      m_Logger.warning("Start index must be at least zero, was " + value + ".");
    } else {
      m_StartIndex = value;
    }
  }

  /**
   * Gets the inter-sample step.
   */
  public int getStep() {
    return m_Step;
  }

  /**
   * Sets the inter-sample step. Must be at least 1.
   */
  public void setStep(int value) {
    if (value < 1) {
      m_Logger.warning("Step must be at least 1, was " + value + ".");
    } else {
      m_Step = value;
    }
  }

  @Override
  public Matrix transform(Matrix predictors) throws Exception {
    // Make sure the start-index is valid for this matrix
    if (m_StartIndex >= predictors.numRows()) {
      throw new RuntimeException("Start index (" +
        m_StartIndex +
        ") is beyond the end of the given matrix (rows = " +
        predictors.numRows() +
        ")"
      );
    }

    // Calculate the number of rows in the output
    int nRows = (predictors.numRows() - 1 - m_StartIndex) / m_Step + 1;

    // Create a matrix to hold the rows
    Matrix result = MatrixFactory.zeros(nRows, predictors.numColumns());

    // Transfer the selected rows to the result
    for (int i = 0; i < nRows; i++) {
      result.setRow(i, predictors.getRow(i * m_Step + m_StartIndex));
    }

    // Return the result
    return result;
  }
}
