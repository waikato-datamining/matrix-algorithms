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
 * PrimitiveUnaryFunctions.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Primitive functions to create in-place operations for matrices.
 *
 * @author Steven Lang
 */
public class PrimitiveUnaryFunctions {

  public static UnaryFunction<Double> add(double value) {
    return PrimitiveMath.ADD.by(value);
  }

  public static UnaryFunction<Double> sub(double value) {
    return PrimitiveMath.SUBTRACT.by(value);
  }

  public static UnaryFunction<Double> mul(double value) {
    return PrimitiveMath.MULTIPLY.by(value);
  }

  public static UnaryFunction<Double> div(double value) {
    return PrimitiveMath.DIVIDE.by(value);
  }

  public static UnaryFunction<Double> pow(double value) {
    return PrimitiveMath.POW.second(value);
  }

}
