package com.github.waikatodatamining.matrix.core;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

import java.util.Random;
import java.util.stream.IntStream;

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

  /**
   * Create an identity matrix, base on the shape of the given matrix.
   * @param other Reference matrix for shape
   * @return Eye like other
   * @see MatrixFactory#eye(int, int)
   */
  public static Matrix eyeLike(Matrix other){
    return eye(other.numRows(), other.numColumns());
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
   * Constructor initializing a new matrix with zeroes with the same shape
   * another matrix.
   *
   * @param other Matrix indicating the shape
   */
  public static Matrix zerosLike(Matrix other) {
    return zeros(other.numRows(), other.numColumns());
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
   * Constructor initializing a new matrix with the same shape like another
   * matrix with a given value.
   *
   * @param other Reference Matrix for shape
   * @param initialValue Initial matrix value for each element
   */
  public static Matrix filledLike(Matrix other, double initialValue) {
    return filled(other.numRows(), other.numColumns(), initialValue);
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

  /**
   * Generate matrix with random elements, sampled from the standard normal distribution of mean 0
   * and variance of 1.
   *
   * @param m       Number of rows
   * @param n       Number of columns
   * @param seed    Seed for the random number generator
   * @return        An m-by-n matrix with gaussian distributed random elements
   */
  public static Matrix randn(int m, int n, long seed) {
    Random rand = new Random(seed);
    double[][] X = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
	X[i][j] = rand.nextGaussian();
      }
    }
    return MatrixFactory.fromRaw(X);
  }

  /**
   * Generate matrix with random elements, sampled from the standard normal
   * distribution of mean 0
   * and variance of 1 with the same shape as the given matrix.
   *
   * @param other Matrix indicating the shape
   * @param seed Seed for the random number generator
   * @return An matrix with gaussian distributed random elements with the shape
   * of {@code other}.
   */
  public static Matrix randnLike(Matrix other, long seed){
    return randn(other.numRows(), other.numColumns(), seed);
  }

  /**
   * Generate matrix with random elements, sampled from the standard normal
   * distribution of the given mean and std.
   *
   * @param m       Number of rows
   * @param n       Number of columns
   * @param seed    Seed for the random number generator
   * @return        An m-by-n matrix with gaussian distributed random elements
   */
  public static Matrix randn(int m, int n, double mean, double std, long seed) {
    Random rand = new Random(seed);
    double[][] X = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
	X[i][j] = rand.nextGaussian() * std + mean;
      }
    }
    return MatrixFactory.fromRaw(X);
  }

  /**
   * Generate matrix with random elements, sampled from the standard normal
   * distribution with the given mean and std and with the same shape as
   * the given matrix.
   *
   * @param other Matrix indicating the shape
   * @param seed Seed for the random number generator
   * @return An matrix with gaussian distributed random elements with the shape
   * of {@code other}.
   */
  public static Matrix randnLike(Matrix other, double mean, double std, long seed){
    return randn(other.numRows(), other.numColumns(), mean, std, seed);
  }

  /**
   * Generate matrix with random elements, sampled from a uniform distribution
   * in (0, 1).
   *
   * @param m       Number of rows
   * @param n       Number of columns
   * @param seed    Seed for the random number generator
   * @return        An m-by-n matrix with uniformly distributed random elements
   */
  public static Matrix rand(int m, int n, long seed) {
    Random rand = new Random(seed);
    double[][] X = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
	X[i][j] = rand.nextDouble();
      }
    }
    return MatrixFactory.fromRaw(X);
  }

  /**
   * Generate matrix with random elements, sampled from a uniform distribution
   * in (0, 1) with the same shape as the given matrix.
   *
   * @param other Matrix indicating the shape
   * @param seed Seed for the random number generator
   * @return An matrix with uniformly distributed random elements with the shape
   * of {@code other}.
   */
  public static Matrix randLike(Matrix other, long seed){
    return rand(other.numRows(), other.numColumns(), seed);
  }

  /**
   * Produce diagonal matrix with given values.
   * @param vector Diagonal values
   * @return Diagonal matrix
   */
  public static Matrix diag(Matrix vector){
    int n = vector.numRows();
    Matrix res = zeros(n, n);
    for (int i = 0; i < n; i++) {
      res.set(i, i, vector.get(i, 0));
    }

    return res;
  }


  /**
   * Create a range matrix.
   *
   * @param rows Number of rows
   * @param columns Number of columns
   * @param start Start value
   * @return Range matrix of given size
   */
  public static Matrix range(int rows, int columns, int start){
    double[] doubles = IntStream.range(start, rows*columns + start).mapToDouble(value -> value).toArray();

    double[][] data = new double[rows][columns];
    int idx = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        data[i][j] = doubles[idx++];
      }
    }

    return fromRaw(data);
  }
}
