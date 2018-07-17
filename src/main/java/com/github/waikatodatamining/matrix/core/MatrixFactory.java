package com.github.waikatodatamining.matrix.core;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

/**
 * Factory for matrix objects.
 */
public class MatrixFactory {

  /** Matrix Factory */
  protected final static PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY =
    PrimitiveDenseStore.FACTORY;

  /**
   * Create a matrix from a given matrix store.
   *
   * @param data Matrix store
   * @return Wrapped matrix store
   */
  protected static Matrix create(MatrixStore<Double> data) {
    return new Matrix(data);
  }


  /**
   * Create a matrix from raw double data.
   *
   * @param data Raw data
   * @return Wrapped data
   */
  protected static Matrix create(double[][] data) {
    return create(FACTORY.rows(data));
  }

  /**
   * Create an n x n identity matrix.
   *
   * @param n Size of the matrix
   * @return Identity matrix
   */
  public static Matrix eye(int n) {
    return eye(n, n);
  }

  /**
   * Create an identity matrix, based on the given sizes. If rows > columns, the
   * lower rows that are out of bound will be zero. Same vice versa with
   * columns.
   *
   * @param rows    Number of rows
   * @param columns Number of columns
   * @return Asymmetrical identity matrix
   */
  public static Matrix eye(int rows, int columns) {
    return create(FACTORY.makeEye(rows, columns));
  }

  public static Matrix fromRaw(double[][] data) {
    return create(FACTORY.rows(data));
  }

  /**
   * Constructor initializing a new matrix with zeroes.
   *
   * @param rows    Number of rows
   * @param columns Number of columns
   */
  public static Matrix zeros(int rows, int columns) {
    return create(FACTORY.makeZero(rows, columns));
  }

  /**
   * Constructor initializing a new matrix with a given value.
   *
   * @param rows         Number of rows
   * @param columns      Number of columns
   * @param initialValue Initial matrix value for each element
   */
  public static Matrix filled(int rows, int columns, double initialValue) {
    PrimitiveDenseStore data = FACTORY.makeFilled(rows, columns, new NullaryFunction<Number>() {
      @Override
      public double doubleValue() {
	return initialValue;
      }

      @Override
      public Number invoke() {
	return initialValue;
      }
    });

    return create(data);
  }


  /**
   * Create a matrix from a given vector.
   *
   * @param vector 1D vector
   * @return Wrapped vector
   */
  protected static Matrix fromRow(Access1D<Double> vector) {
    return new Matrix(FACTORY.rows(vector));
  }

  /**
   * Create a matrix from a given raw data vector.
   *
   * @param vector Raw data vector
   * @return Wrapped raw data
   */
  public static Matrix fromRow(double[] vector) {
    return new Matrix(FACTORY.rows(vector));
  }

  /**
   * Create a matrix from a given vector.
   *
   * @param vector 1D vector
   * @return Wrapped vector
   */
  protected static Matrix fromColumn(Access1D<Double> vector) {
    return new Matrix(FACTORY.columns(vector));
  }

  /**
   * Create a matrix from a given raw data vector.
   *
   * @param vector Raw data vector
   * @return Wrapped raw data
   */
  public static Matrix fromColumn(double[] vector) {
    return new Matrix(FACTORY.columns(vector));
  }
}
