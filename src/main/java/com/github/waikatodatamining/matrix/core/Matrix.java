package com.github.waikatodatamining.matrix.core;

import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixInversionException;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.ColumnView;
import org.ojalgo.access.RowView;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.scalar.ComplexNumber;

import java.util.Objects;

/**
 * Matrix abstraction to the ojAlgo's Matrix PrimitiveDenseStore implementation.
 *
 * @author Steven Lang
 */
public class Matrix {

  /** Matrix Factory */
  private final static PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY =
    PrimitiveDenseStore.FACTORY;

  /** Underlying data store */
  protected MatrixStore<Double> data;

  /**
   * Eigenvalue decomposition. Get reset after {@link #data} has changed.
   */
  protected Eigenvalue<Double> eigenvalueDecomposition;

  /**
   * SingularValue decomposition. Get reset after {@link #data} has changed.
   */
  protected SingularValue<Double> singularvalueDecomposition;

  /**
   * Constructor for creating a new matrix wrapper from another matrix store.
   *
   * @param data Matrix store
   */
  private Matrix(MatrixStore<Double> data) {
    this.data = data;
  }

  /**
   * Constructor for creating a new matrix wrapper from a raw 2d double array.
   *
   * @param data Raw data
   */
  public Matrix(double[][] data) {
    this.data = FACTORY.rows(data);
  }

  /**
   * Constructor initializing a new matrix with zeroes.
   *
   * @param rows    Number of rows
   * @param columns Number of columns
   */
  public Matrix(int rows, int columns) {
    data = FACTORY.makeZero(rows, columns);
  }

  /**
   * Constructor initializing a new matrix with a given value.
   *
   * @param rows         Number of rows
   * @param columns      Number of columns
   * @param initialValue Initial matrix value for each element
   */
  public Matrix(int rows, int columns, double initialValue) {
    data = FACTORY.makeFilled(rows, columns, new NullaryFunction<Number>() {
      @Override
      public double doubleValue() {
	return initialValue;
      }

      @Override
      public Number invoke() {
	return initialValue;
      }
    });
  }

  /**
   * Get the submatrix, given by the row and column indices.
   *
   * @param rows    Row indices
   * @param columns Column indices
   * @return Submatrix of the current matrix
   */
  public Matrix getSubMatrix(int[] rows, int[] columns) {
    // Select rows
    Access1D[] rowVectors = new Access1D[rows.length];
    for (int i = 0; i < rows.length; i++) {
      rowVectors[i] = data.sliceRow(rows[i]);
    }
    MatrixStore<Double> rows1 = FACTORY.rows(rowVectors);

    // Select columns
    Access1D[] columnVectors = new Access1D[columns.length];
    for (int j = 0; j < columns.length; j++) {
      columnVectors[j] = rows1.sliceColumn(columns[j]);
    }

    MatrixStore<Double> subMatrix = FACTORY.columns(columnVectors);
    return create(subMatrix);
  }

  /**
   * Get the submatrix, given by the row and column intervals.
   *
   * @param rowStart    Row interval start
   * @param rowEnd      Row interval end
   * @param columnStart Column interval start
   * @param columnEnd   Column interval end
   * @return Submatrix of the current matrix
   */
  public Matrix getSubMatrix(int rowStart, int rowEnd, int columnStart, int columnEnd) {
    int numRows = (rowEnd + 1) - rowStart;
    int numColumns = (columnEnd + 1) - columnStart;
    int[] rows = new int[numRows];
    int[] columns = new int[numColumns];

    int rowIdx = rowStart;
    for (int i = 0; i < numRows; i++) {
      rows[i] = rowIdx;
      rowIdx++;
    }

    int colIdx = columnStart;
    for (int j = 0; j < numColumns; j++) {
      columns[j] = colIdx;
      colIdx++;
    }

    return getSubMatrix(rows, columns);
  }

  /**
   * Get the eigenvectors of this matrix.
   *
   * @return Eigenvectors of this matrix
   */
  public Matrix getEigenvectors() {
    if (eigenvalueDecomposition == null) {
      eigenvalueDecomposition = Eigenvalue.PRIMITIVE.make(data);
    }
    return create(eigenvalueDecomposition.getV());
  }

  /**
   * Get the eigenvalues of this matrix.
   *
   * @return Eigenvalues of this matrix
   */
  public Matrix getEigenvalues() {
    if (eigenvalueDecomposition == null) {
      eigenvalueDecomposition = Eigenvalue.PRIMITIVE.make(data);
    }
    Array1D<ComplexNumber> eigenvalues = eigenvalueDecomposition.getEigenvalues();
    double[] doubles = eigenvalues.toRawCopy1D();
    return fromColumn(doubles);
  }

  /**
   * Get the U matrix of the SVD decomposition of this matrix.
   *
   * @return SVD-U matrix
   */
  public Matrix svdU() {
    // TODO: convert to ojAlgo SVD
    double[][] data = this.data.toRawCopy2D();
    return create(new Jama.Matrix(data).svd().getU().getArray());
  }

  /**
   * Get the V matrix of the SVD decomposition of this matrix.
   *
   * @return SVD-V matrix
   */
  public Matrix svdV() {
    // TODO: convert to ojAlgo SVD
    double[][] data = this.data.toRawCopy2D();
    return create(new Jama.Matrix(data).svd().getV().getArray());
  }

  /**
   * Calculate the l2-norm of this matrix.
   *
   * @return L2 norm
   */
  public double norm2() {
    return data.norm();
  }

  /**
   * Multiply this matrix with another matrix.
   *
   * @param other Multiplicand
   * @return Matrix multiplication result
   */
  public Matrix mul(Matrix other) {
    return create(data.multiply(other.data));
  }

  /**
   * Multiply each element of this matrix with a scalar.
   *
   * @param scalar Scalar value
   * @return This matrix with each element multiplied by the given scalar
   */
  public Matrix mul(double scalar) {
    return create(data.multiply(scalar));
  }

  /**
   * Divide each element of this matrix by a scalar.
   *
   * @param scalar Scalar value
   * @return This matrix with each element divided by the given scalar
   */

  public Matrix div(double scalar) {
    return create(data.multiply(1.0 / scalar));
  }

  /**
   * Substract the given matrix from this matrix.
   *
   * @param other Subtrahend
   * @return Result of the other matrix substracted from this matrix
   */
  public Matrix sub(Matrix other) {
    return create(data.subtract(other.data));
  }

  /**
   * Add the given matrix to this matrix.
   *
   * @param other Other matrix
   * @return Result of the addition
   */
  public Matrix add(Matrix other) {
    return create(data.add(other.data));
  }

  /**
   * Transpose this matrix.
   *
   * @return This matrix, transposed
   */
  public Matrix transpose() {
    return create(data.transpose());
  }

  /**
   * Number of columns.
   *
   * @return Number of columns
   */
  public int numColumns() {
    return (int) data.countColumns();
  }

  /**
   * Number of rows.
   *
   * @return Number of rows
   */
  public int numRows() {
    return (int) data.countRows();
  }

  /**
   * Get a value in this matrix.
   *
   * @param row    Row index
   * @param column Column index
   * @return Scalar at the given position
   */
  public double get(int row, int column) {
    return data.get(row, column);
  }

  /**
   * Set a value in this matrix.
   *
   * @param row    Row index
   * @param column Column index
   * @param value  Scalar
   */
  public void set(int row, int column, double value) {
    if (data instanceof PhysicalStore) {
      ((PhysicalStore<Double>) data).set(row, column, value);
    }
    else {
      PhysicalStore<Double> copy = data.copy();
      copy.set(row, column, value);
      data = copy;
    }
    resetCache();
  }

  /**
   * Set a row in this matrix. If this matrix is a physical store, the operation
   * will be inplace, else the operation first creates a copy which will be
   * modified and replaces the current matrix store (more memory intensive).
   *
   * @param rowIdx Row index
   * @param row    Row
   */
  public void setRow(int rowIdx, Matrix row) {
    if (data instanceof PhysicalStore) {
      ((PhysicalStore<Double>) data).fillRow(rowIdx, row.data);
    }
    else {
      PhysicalStore<Double> copy = data.copy();
      copy.fillRow(rowIdx, row.data);
      data = copy;
    }
    resetCache();
  }

  /**
   * Set a column in this matrix. If this matrix is a physical store, the operation
   * will be inplace, else the operation first creates a copy which will be
   * modified and replaces the current matrix store (more memory intensive).
   *
   * @param columnIdx Row index
   * @param column    Row
   */
  public void setColumn(int columnIdx, Matrix column) {
    if (data instanceof PhysicalStore) {
      ((PhysicalStore<Double>) data).fillColumn(columnIdx, column.data);
    }
    else {
      PhysicalStore<Double> copy = data.copy();
      copy.fillColumn(columnIdx, column.data);
      data = copy;
    }
    resetCache();
  }

  /**
   * Get a row from this matrix.
   *
   * @param rowIdx Row index
   * @return Row at the given index
   */
  public Matrix getRow(int rowIdx) {
    return fromRow(data.sliceRow(rowIdx));
  }

  /**
   * Get a column from this matrix.
   *
   * @param columnidx Column index
   * @return Column at the given index
   */
  public Matrix getColumn(int columnidx) {
    return fromColumn(data.sliceColumn(columnidx));
  }

  /**
   * Invert this matrix.
   *
   * @return Inverted matrix
   */
  public Matrix inverse() {
    InverterTask<Double> task = InverterTask.PRIMITIVE.make(data);
    try {
      return create(task.invert(data));
    }
    catch (RecoverableCondition recoverableCondition) {
      throw new MatrixInversionException("", recoverableCondition);
    }
  }

  /**
   * Create a duplicate of this matrix.
   *
   * @return Duplicate of this matrix
   */
  public Matrix copy() {
    return create(data.copy());
  }

  /**
   * Copy this matrix to a raw 1D double array.
   *
   * @return Raw 1D double array of this matrix
   */
  public double[] toRawCopy1D() {
    return data.toRawCopy1D();
  }

  /**
   * Copy this matrix to a raw 2D double array.
   *
   * @return Raw 2D double array of this matrix
   */
  public double[][] toRawCopy2D() {
    return data.toRawCopy2D();
  }

  /**
   * Concatenate this matrix with another over the given axis.
   *
   * @param other Other matrix which will be appended to this matrix
   * @param axis  Concatenation axis
   * @return Concatenated matrices
   */
  public Matrix concat(Matrix other, int axis) {
    if (axis == 0) {
      return concatAlongRows(other);
    }
    else if (axis == 1) {
      return concatAlongColumns(other);
    }
    else {
      throw new MatrixAlgorithmsException("Axis must be 0 (rows) or 1 (columns) but was " + axis);
    }
  }

  /**
   * Concatenate this matrix with another along rows.
   *
   * @param other Other matrix which will be appended to this matrix
   * @return Concatenated matrices
   */
  public Matrix concatAlongRows(Matrix other) {
    RowView[] vectors = new RowView[this.numRows() + other.numRows()];
    int count = 0;
    for (RowView<Double> row : data.rows()) {
      vectors[count] = row;
      count++;
    }

    for (RowView<Double> row : other.data.rows()) {
      vectors[count] = row;
      count++;
    }

    return create(FACTORY.rows(vectors));
  }

  /**
   * Concatenate this matrix with another along columns.
   *
   * @param other Other matrix which will be appended to this matrix
   * @return Concatenated matrices
   */
  public Matrix concatAlongColumns(Matrix other) {
    ColumnView[] vectors = new ColumnView[this.numRows() + other.numRows()];
    int count = 0;
    for (ColumnView<Double> row : data.columns()) {
      vectors[count] = row;
      count++;
    }

    for (ColumnView<Double> row : other.data.columns()) {
      vectors[count] = row;
      count++;
    }

    return create(FACTORY.columns(vectors));
  }

  /**
   * Reset matrix cache, that is different decompositions that can be cached
   * the matrix has been modified.
   */
  protected void resetCache() {
    this.eigenvalueDecomposition = null;
    this.singularvalueDecomposition = null;
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
   * Create a matrix from a given matrix store.
   *
   * @param data Matrix store
   * @return Wrapped matrix store
   */
  private static Matrix create(MatrixStore<Double> data) {
    return new Matrix(data);
  }

  /**
   * Create a matrix from a given vector.
   *
   * @param vector 1D vector
   * @return Wrapped vector
   */
  private static Matrix fromRow(Access1D<Double> vector) {
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
  private static Matrix fromColumn(Access1D<Double> vector) {
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
   * Create an n x n identity matrix.
   *
   * @param n Size of the matrix
   * @return Identity matrix
   */
  public static Matrix identity(int n) {
    return identity(n, n);
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
  public static Matrix identity(int rows, int columns) {
    return create(FACTORY.makeEye(rows, columns));
  }

  /**
   * Check if this matrix is a row vector.
   *
   * @return True if this matrix is a row vector
   */
  public boolean isRowVector() {
    return data.isVector() && numRows() == 1;
  }

  /**
   * Check if this matrix is a column vector.
   *
   * @return True if this matrix is a column vector
   */
  public boolean isColumnVector() {
    return data.isVector() && numColumns() == 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Matrix matrix = (Matrix) o;
    return Objects.equals(data, matrix.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
