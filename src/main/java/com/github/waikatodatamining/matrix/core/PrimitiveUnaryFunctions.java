package com.github.waikatodatamining.matrix.core;

import org.ojalgo.function.PrimitiveFunction.Unary;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Primitive functions to create inplace operations for Matrices.
 *
 * @author Steven Lang
 */
public class PrimitiveUnaryFunctions {

  public static UnaryFunction<Double> add(double value) {
    //return (Unary) arg -> arg + value;
    return PrimitiveMath.ADD.by(value);
  }

  public static UnaryFunction<Double> sub(double value) {
    //return (Unary) arg -> arg - value;
    return PrimitiveMath.SUBTRACT.by(value);
  }

  public static UnaryFunction<Double> mul(double value) {
    //return (Unary) arg -> arg * value;
    return PrimitiveMath.MULTIPLY.by(value);
  }

  public static UnaryFunction<Double> div(double value) {
    //return (Unary) arg -> arg / value;
    return PrimitiveMath.DIVIDE.by(value);
  }

  public static UnaryFunction<Double> pow(double value) {
    return (Unary) arg -> StrictMath.pow(arg, value);
    //return PrimitiveMath.POW.second(value);
  }

}
