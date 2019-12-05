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
 * MatrixInversionException.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.exceptions;

/**
 * Exception for when a matrix cannot be inverted.
 */
public class MatrixInversionException
  extends MatrixAlgorithmsException {

  private static final long serialVersionUID = -6704402669137006394L;

  private static final String PREFIX = "Could not invert matrix. ";

  public MatrixInversionException(String message) {
    super(PREFIX + message);
  }

  public MatrixInversionException(String message, Throwable cause) {
    super(PREFIX + message, cause);
  }
}
