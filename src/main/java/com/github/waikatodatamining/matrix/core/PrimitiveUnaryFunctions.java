package com.github.waikatodatamining.matrix.core;

import org.ojalgo.function.PrimitiveFunction.Unary;
import org.ojalgo.function.UnaryFunction;

/**
 * Primitive functions to create inplace operations for Matrices.
 *
 * @author Steven Lang
 */
public class PrimitiveUnaryFunctions {

  public static UnaryFunction<Double> add(double value) {
    return (Unary) arg -> arg + value;
  }

  public static UnaryFunction<Double> sub(double value) {
    return (Unary) arg -> arg - value;
  }

  public static UnaryFunction<Double> mul(double value) {
    return (Unary) arg -> arg * value;
  }

  public static UnaryFunction<Double> div(double value) {
    return (Unary) arg -> arg / value;
  }

  public static UnaryFunction<Double> pow(double value) {
    return (Unary) arg -> StrictMath.pow(arg, value);
  }

}
