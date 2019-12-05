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
 * InvalidShapeException.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.exceptions;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;

import java.util.Arrays;

/**
 * Exception for processes that require matrices to have particular
 * numbers of rows/columns, and they don't.
 */
public class InvalidShapeException extends MatrixAlgorithmsException{

  private static final long serialVersionUID = 3673145587255476375L;

  public InvalidShapeException(String message) {
    super("Invalid shape " + message);
  }

  public InvalidShapeException(String message, Matrix... matrices) {
    super("Invalid shapes "
      + Arrays.stream(matrices).map(Matrix::shapeString).reduce((s, s2) -> s + ", " + s2).get()
      + " "
      + message);
  }
}
