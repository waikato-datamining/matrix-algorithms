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
 * AbstractSingleResponsePLS.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;

/**
 * Ancestor for PLS algorithms that work on multiple response variables.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractMultiResponsePLS
  extends AbstractPLS {

  private static final long serialVersionUID = -8160023117935320371L;

  /**
   * Returns the minimum number of columns the response matrix has to have.
   *
   * @return		the minimum
   */
  protected abstract int getMinColumnsResponse();

  /**
   * Returns the maximum number of columns the response matrix has to have.
   *
   * @return		the maximum, -1 for unlimited
   */
  protected abstract int getMaxColumnsResponse();

  /**
   * Initializes using the provided data.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   */
  @Override
  protected void doConfigure(Matrix predictors, Matrix response) {
    if (response.numColumns() < getMinColumnsResponse())
      throw new MatrixAlgorithmsException("Algorithm requires at least " +
            getMinColumnsResponse() +
            " response columns, found: " +
            response.numColumns());
    else if ((getMaxColumnsResponse() != -1) && (response.numColumns() > getMaxColumnsResponse()))
      throw new MatrixAlgorithmsException("Algorithm can handle at most " +
            getMaxColumnsResponse() +
            " response columns, found: " +
            response.numColumns());

    super.doConfigure(predictors, response);
  }
}
