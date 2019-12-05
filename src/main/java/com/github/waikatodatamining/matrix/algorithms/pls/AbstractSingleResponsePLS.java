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
 * Ancestor for PLS algorithms that work on a single response variable.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSingleResponsePLS
  extends AbstractPLS {

  private static final long serialVersionUID = -8160023117935320371L;

  @Override
  protected void doConfigure(Matrix X, Matrix y) {
    if (y.numColumns() != 1)
      throw new MatrixAlgorithmsException("Algorithm requires exactly one response variable, found: " + y.numColumns());

    super.doConfigure(X, y);
  }
}
