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
 * MultiFilter.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.algorithm.MatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.exceptions.InverseTransformException;

import java.util.List;

/**
 * Filter which encapsulates a series of sub-filters, and applies
 * them in a given order.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class MultiFilter
  extends MatrixAlgorithm {

  // The filters in order of application
  protected List<MatrixAlgorithm> m_Algorithms;

  public MultiFilter(List<MatrixAlgorithm> algorithms) {
    m_Algorithms = algorithms;
  }

  @Override
  public Matrix doTransform(Matrix predictors) {
    // The result starts as the predictors
    Matrix result = predictors;

    // Apply each filter in ordered turn
    for (MatrixAlgorithm algorithm : m_Algorithms) {
      if (algorithm instanceof UnsupervisedMatrixAlgorithm)
        result = ((UnsupervisedMatrixAlgorithm) algorithm).configureAndTransform(result);
      else
        result = algorithm.transform(result);
    }

    return result;
  }

  @Override
  protected Matrix doInverseTransform(Matrix matrix) throws InverseTransformException {
    // TODO
    return super.doInverseTransform(matrix);
  }

  @Override
  public boolean isNonInvertible() {
    // We're non-invertible if any sub-algorithm is non-invertible
    for (MatrixAlgorithm algorithm : m_Algorithms)
      if (algorithm.isNonInvertible())
        return true;

    return false;
  }
}
