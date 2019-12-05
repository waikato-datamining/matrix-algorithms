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
 * UninvertibleAlgorithmException.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.exceptions;

import com.github.waikatodatamining.matrix.core.algorithm.MatrixAlgorithm;

/**
 * Exception when trying to inverse-tranform data using an algorithm
 * that cannot be inverted under any circumstance.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class UninvertibleAlgorithmException
  extends InverseTransformException {

  private static final long serialVersionUID = -3731391195508104892L;

  public <T extends MatrixAlgorithm> UninvertibleAlgorithmException(Class<T> algorithm) {
    super("Algorithm " + algorithm.getName() + " is not invertible");
  }

}
