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
 * InverseTransformException.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.exceptions;

/**
 * Base class for exceptions when trying to perform the inverse
 * transformation of an algorithm.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class InverseTransformException
  extends MatrixAlgorithmsException {

  private static final long serialVersionUID = 7560545977616671805L;

  public InverseTransformException(String message) {
    super(message);
  }

  public InverseTransformException(String message, Throwable cause) {
    super(message, cause);
  }

}
