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
 * MatrixAlgorithmsException.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.exceptions;

/**
 * Base class for all custom exceptions coming from this library.
 * Inherits from RuntimeException so all are unchecked.
 */
public class MatrixAlgorithmsException
  extends RuntimeException {

  private static final long serialVersionUID = -3583358235821258766L;

  public MatrixAlgorithmsException(String message) {
    super(message);
  }

  public MatrixAlgorithmsException(String message, Throwable cause) {
    super(message, cause);
  }
}
