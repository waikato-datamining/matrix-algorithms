package com.github.waikatodatamining.matrix.core;

import com.github.waikatodatamining.matrix.core.exceptions.InvalidAxisException;
import com.github.waikatodatamining.matrix.core.exceptions.InvalidShapeException;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixInversionException;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.github.waikatodatamining.matrix.core.MatrixFactory.create;

/**
 * Matrix abstraction to the ojAlgo's Matrix PrimitiveDenseStore implementation.
 *
 * @author Steven Lang
 */
public class Matrix {


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
  protected Matrix(MatrixStore<Double> data) {
    this.data = data;
  }

  /**
   * Get the submatrix, given by the row and column indices.
   *
   * @param rows    Row indices
   * @param columns Column indices
   * @return Submatrix of the current matrix
   */
  public Matrix getSubMatrix(int[] rows, int[] columns) {
    double[][] dataRaw = data.toRawCopy2D();
    double[][] subset = new double[rows.length][columns.length];
    int newRowIdx = 0;
    int newColumnIdx = 0;
    for (int row : rows) {
      for (int column : columns) {
	subset[newRowIdx][newColumnIdx] = dataRaw[row][column];
	newColumnIdx++;
      }
      newColumnIdx = 0;
      newRowIdx++;
    }

    return create(subset);
  }

  /**
   * Get the submatrix, given by the row and column intervals.
   *
   * @param rowStart           Row interval start
   * @param rowEndExclusive    Row interval end exclusive
   * @param columnStart        Column interval start
   * @param columnEndExclusive Column interval end exclusive
   * @return Submatrix of the current matrix
   */
  public Matrix getSubMatrix(int rowStart, int rowEndExclusive, int columnStart, int columnEndExclusive) {
    int numRows = rowEndExclusive - rowStart;
    int numColumns = columnEndExclusive - columnStart;
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
   * Get the eigenvectors of this matrix, sorted according to their descending eigenvalues.
   *
   * @param sortDominance If true, the columns are sorted according to the vectors dominances
   * @return Eigenvectors of this matrix
   */
  public Matrix getEigenvectors(boolean sortDominance) {
    makeEigenvalueDecomposition();
    MatrixStore<Double> eigVunsorted = eigenvalueDecomposition.getV();

    if (sortDominance) {
      double[] eigVals = eigenvalueDecomposition.getEigenvalues().toRawCopy1D();
      int[] sortedColumnIndices = IntStream
	.range(0, eigVals.length)
	.boxed()
	.sorted(Comparator.comparingDouble(o -> -1 * eigVals[o])) // Multiply by -1 for descending order
	.mapToInt(i -> i)
	.toArray();
      int[] allRows = IntStream.range(0, (int) eigVunsorted.countRows()).toArray();
      Matrix eigVsorted = MatrixFactory.create(eigVunsorted).getSubMatrix(allRows, sortedColumnIndices);
      return eigVsorted;
    }
    else {
      return MatrixFactory.create(eigVunsorted);
    }
  }

  /**
   * Get the eigenvectors of this matrix, sorted according to their ascending eigenvalues.
   *
   * @return Eigenvectors of this matrix
   */
  public Matrix getEigenvectorsSortedAscending() {
    makeEigenvalueDecomposition();
    MatrixStore<Double> eigVunsorted = eigenvalueDecomposition.getV();

    double[] eigVals = eigenvalueDecomposition.getEigenvalues().toRawCopy1D();
    int[] sortedColumnIndices = IntStream
      .range(0, eigVals.length)
      .boxed()
      .sorted(Comparator.comparingDouble(o -> eigVals[o])) // Multiply by -1 for descending order
      .mapToInt(i -> i)
      .toArray();
    int[] allRows = IntStream.range(0, (int) eigVunsorted.countRows()).toArray();
    Matrix eigVsorted = MatrixFactory.create(eigVunsorted).getSubMatrix(allRows, sortedColumnIndices);
    return eigVsorted;
  }

  /**
   * Get the eigenvectors of this matrix.
   *
   * @return Eigenvectors of this matrix
   */
  public Matrix getEigenvectors() {
    return getEigenvectors(false);
  }

  public Matrix getEigenvalueDecompositionV() {
    makeEigenvalueDecomposition();
    return MatrixFactory.create(eigenvalueDecomposition.getV());
  }

  public Matrix getEigenvalueDecompositionD() {
    makeEigenvalueDecomposition();
    return MatrixFactory.create(eigenvalueDecomposition.getD());
  }

  /**
   * Get the eigenvalues of this matrix.
   *
   * @return Eigenvalues of this matrix
   */
  public Matrix getEigenvalues() {
    makeEigenvalueDecomposition();
    Array1D<ComplexNumber> eigenvalues = eigenvalueDecomposition.getEigenvalues();
    return MatrixFactory.fromColumn(eigenvalues.toRawCopy1D());
  }

  /**
   * Get the descending sorted eigenvalues of this matrix.
   *
   * @return Eigenvalues of this matrix
   */
  public Matrix getEigenvaluesSortedDescending() {
    makeEigenvalueDecomposition();
    Array1D<ComplexNumber> eigenvalues = eigenvalueDecomposition.getEigenvalues();
    eigenvalues.sortDescending();
    return MatrixFactory.fromColumn(eigenvalues.toRawCopy1D());
  }

  /**
   * Get the descending sorted eigenvalues of this matrix.
   *
   * @return Eigenvalues of this matrix
   */
  public Matrix getEigenvaluesSortedAscending() {
    makeEigenvalueDecomposition();
    Array1D<ComplexNumber> eigenvalues = eigenvalueDecomposition.getEigenvalues();
    eigenvalues.sortAscending();
    return MatrixFactory.fromColumn(eigenvalues.toRawCopy1D());
  }

  /**
   * Initialize the eigenvalue decomposition.
   */
  protected void makeEigenvalueDecomposition() {
    if (eigenvalueDecomposition == null) {
      eigenvalueDecomposition = Eigenvalue.PRIMITIVE.make(data);
      eigenvalueDecomposition.decompose(data);
    }
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
   * Get the V matrix of the SVD decomposition of this matrix.
   *
   * @return SVD-V matrix
   */
  public Matrix svdS() {
    // TODO: convert to ojAlgo SVD
    double[][] data = this.data.toRawCopy2D();
    return create(new Jama.Matrix(data).svd().getS().getArray());
  }

  /**
   * Compute the sum over a certain axis.
   *
   * @param axis Indicating axis index
   * @return Sum over axis
   */
  public Matrix sum(int axis) {
    if (axis == -1) { // Sum over all
      return create(
	data
	  .reduceRows(Aggregator.SUM).get()
	  .reduceColumns(Aggregator.SUM).get()
      );
    }
    else if (axis == 0) { // Sum over rows
      Matrix result = MatrixFactory.zeros(1, numColumns());

      for (int i = 0; i < numColumns(); i++) {
	result.set(0, i, data.aggregateColumn(i, Aggregator.SUM));
      }

      return result;
    }
    else if (axis == 1) { // Sum over columns
      Matrix result = MatrixFactory.zeros(numRows(), 1);
      for (int i = 0; i < numRows(); i++) {
	result.set(i, 0, data.aggregateRow(i, Aggregator.SUM));
      }

      return result;
    }
    else {
      throw new InvalidAxisException(axis);
    }
  }

  /**
   * Calculate the l1-norm of this matrix.
   *
   * @return L1 norm
   */
  public double norm1() {
    return data.aggregateAll(Aggregator.NORM1);
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
   * Calculate the squared l2-norm of this matrix.
   *
   * @return Squared l2 norm
   */
  public double norm2squared() {
    double norm2 = data.norm();
    return norm2 * norm2;
  }

  /**
   * Multiply this matrix with another matrix.
   *
   * @param other Multiplicand
   * @return Matrix multiplication result
   */
  public Matrix mul(Matrix other) {
    // Check for matching shapes
    if (numColumns() != other.numRows()) {
      throw new InvalidShapeException("Invalid matrix multiplication. Shapes " +
	"do not match.");
    }
    return create(data.multiply(other.data));
  }

  /**
   * Read data from another matrix and assign it to itself.
   *
   * @param other Other matrix to read the data from
   */
  public void assign(Matrix other) {
    other.data.supplyTo(physical());
  }

  /**
   * Check if the underlying matrix store is physical and with that mutable.
   *
   * @return True if {@link Matrix#data} is instance of {@link PhysicalStore}
   */
  protected boolean isPhysicalStore() {
    return data instanceof PhysicalStore;
  }

  /**
   * Check if the matrix multiplication between this and the other matrix can be
   * done regarding shapes.
   *
   * @param other Other matrix
   * @return True if multiplication is valid
   */
  public boolean isMultiplicableWith(Matrix other) {
    return this.numColumns() == other.numRows();
  }

  /**
   * Compute the vector dot product. Equivalent to x.transpose().mul(y) but
   * faster as transposition is skipped.
   * Both vectors (this and other) have to be in the
   * shape [1 x n] to skip the explicit transposition.
   *
   * @param other Other vector
   * @return Vector dot product
   */
  public double vectorDot(Matrix other) {
    if (isVector() && sameShapeAs(other)) {
      return data.dot(other.data);
    }
    else {
      throw new InvalidShapeException("Shape " + shapeString() + " and " +
	"" + other.shapeString() + " are incompatible for vector product. " +
	"Shape [ 1 x n ] has to be ensured on both vectors.");
    }
  }

  /**
   * Return the matrix, normalized over the columns.
   *
   * @return Normalized matrix
   */
  public Matrix normalized() {
    return normalized(0);
  }

  /**
   * Get the normalized matrix based on a specific normalization axis.
   *
   * @param axis Normalization axis
   * @return Normalized matrix
   */
  public Matrix normalized(int axis) {
    Matrix result = copy();
    if (axis == 0) {
      MatrixStore<Double> supplier = data.reduceColumns(Aggregator.NORM2).get();
      for (int j = 0; j < numColumns(); j++) {
	Double norm = supplier.get(j);
	result.setColumn(j, getColumn(j).div(norm));
      }
    }
    else if (axis == 1) {
      MatrixStore<Double> supplier = data.reduceRows(Aggregator.NORM2).get();
      for (int i = 0; i < numRows(); i++) {
	Double norm = supplier.get(i);
	result.setRow(i, getRow(i).div(norm));
      }
    }
    else {
      throw new MatrixAlgorithmsException("Invalid axis for normalization. " +
	"Must be either 0 or 1 but was " + axis);
    }

    return result;
  }

  /**
   * Check if this matrix is a vector.
   *
   * @return True if either {@link Matrix#numRows()} equals 1 or
   * {@link Matrix#numColumns()} equals 1
   */
  private boolean isVector() {
    return data.isVector();
  }

  /**
   * Check if this matrix has the same shape as a second matrix.
   *
   * @param other Matrix to be compared
   * @return True if shapes are the same
   */
  private boolean sameShapeAs(Matrix other) {
    return numRows() == other.numRows() && numColumns() == other.numColumns();
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
   * Multiply each element of this matrix with a scalar in place.
   *
   * @param scalar Scalar value
   * @return This matrix with each element multiplied by the given scalar
   */
  public Matrix muli(double scalar) {
    physical().modifyAll(PrimitiveUnaryFunctions.mul(scalar));
    return this;
  }


  /**
   * Multiply each element of this matrix with a the element at the same index
   * in the other matrix.
   *
   * @param other Other matrix
   * @return This matrix with each element multiplied by the element at the same
   * index in the other matrix
   */
  public Matrix mulElementwise(Matrix other) {
    return create(data.operateOnMatching(PrimitiveFunction.MULTIPLY, other.data).get());
  }

  /**
   * Scale the i-th column of this matrix by the i-th element of the input
   * vector.
   *
   * @param vector Scale input vector
   * @return Scaled matrix
   */
  public Matrix scaleByRowVector(Matrix vector) {
    if (!vector.isVector()) {
      throw new InvalidShapeException("Parameter vector was not a vector. " +
	"Actual shape: " + vector.shapeString());
    }

    if (numColumns() != vector.numRows()) {
      throw new InvalidShapeException("Second dimension of the matrix and size of" +
	"vector has to match. Matrix shape: " + shapeString() + ", vector " +
	"shape: " + vector.shapeString());
    }

    Matrix result = copy();

    for (int j = 0; j < numColumns(); j++) {
      Matrix col = getColumn(j);
      double scalar = vector.get(j, 0);
      Matrix scaledCol = col.mul(scalar);
      result.setColumn(j, scaledCol);
    }

    return result;
  }

  /**
   * Scale the i-th row of this matrix by the i-th element of the input
   * vector.
   *
   * @param vector Scale input vector
   * @return Scaled matrix
   */
  public Matrix scaleByColumnVector(Matrix vector) {
    if (!vector.isVector()) {
      throw new InvalidShapeException("Parameter vector was not a vector. " +
	"Actual shape: " + vector.shapeString());
    }

    if (numRows() != vector.numRows()) {
      throw new InvalidShapeException("First dimension of the matrix and size of" +
	"vector has to match. Matrix shape: " + shapeString() + ", vector " +
	"shape: " + vector.shapeString());
    }

    Matrix result = MatrixFactory.zerosLike(this);

    for (int i = 0; i < numRows(); i++) {
      Matrix row = getRow(i);
      double scalar = vector.get(i, 0);
      Matrix scaledRow = row.mul(scalar);
      result.setRow(i, scaledRow);
    }

    return result;
  }


  /**
   * Add the i-th element of the input vector each element of the i-th
   * column of this matrix.
   *
   * @param vector Add input vector
   * @return Matrix
   */
  public Matrix addByVector(Matrix vector) {
    if (!vector.isVector()) {
      throw new InvalidShapeException("Parameter vector was not a vector. " +
	"Actual shape: " + vector.shapeString());
    }

    if (numColumns() != vector.numRows()) {
      throw new InvalidShapeException("Second dimension of the matrix and sie of" +
	"vector has to match. Matrix shape: " + shapeString() + ", vector " +
	"shape: " + vector.shapeString());
    }

    Matrix result = copy();

    for (int j = 0; j < numColumns(); j++) {
      Matrix col = getColumn(j);
      double scalar = vector.get(j, 0);
      Matrix scaledCol = col.add(scalar);
      result.setColumn(j, scaledCol);
    }

    return result;
  }


  /**
   * Multiply each element of this matrix with a the element at the same index
   * in the other matrix.
   *
   * @param other Other matrix
   * @return This matrix with each element multiplied by the element at the same
   * index in the other matrix
   */
  public Matrix divElementwise(Matrix other) {
    return create(data.operateOnMatching(PrimitiveFunction.DIVIDE, other.data).get());
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
   * Divide each element of this matrix by a scalar in place.
   *
   * @param scalar Scalar value
   * @return This matrix with each element divided by the given scalar
   */

  public Matrix divi(double scalar) {
    physical().modifyAll(PrimitiveUnaryFunctions.div(scalar));
    return this;
  }

  /**
   * Subtract the given matrix from this matrix.
   *
   * @param other Subtrahend
   * @return Result of the other matrix subtracted from this matrix
   */
  public Matrix sub(Matrix other) {
    if (sameShapeAs(other)) {
      return create(data.subtract(other.data));
    }
    else {
      throw new InvalidShapeException("", this, other);
    }
  }

  /**
   * Add the given matrix to this matrix.
   *
   * @param other Other matrix
   * @return Result of the addition
   */
  public Matrix add(Matrix other) {
    if (sameShapeAs(other)) {
      return create(data.add(other.data));
    }
    else {
      throw new InvalidShapeException("", this, other);
    }

  }

  /**
   * Add the given scalar to each element of this matrix.
   *
   * @param value Scalar value
   * @return Result of the addition
   */
  public Matrix add(double value) {
    Matrix filled = MatrixFactory.filled(numRows(), numColumns(), value);
    return add(filled);
  }

  /**
   * Add the given scalar inplace to each element of this matrix.
   *
   * @param value Scalar value
   * @return This matrix
   */
  public Matrix addi(double value) {
    physical().modifyAll(PrimitiveUnaryFunctions.add(value));
    return this;
  }

  /**
   * Add the given matrix inplace to this matrix.
   *
   * @param other Matrix to add
   * @return This matrix
   */
  public Matrix addi(Matrix other) {
    physical().modifyMatching(PrimitiveFunction.ADD, other.data);
    return this;
  }

  /**
   * Subtract the given scalar from each element of this matrix.
   *
   * @param value Scalar value
   * @return Result of the subtraction
   */
  public Matrix sub(double value) {
    Matrix filled = MatrixFactory.filled(numRows(), numColumns(), value);
    return sub(filled);
  }

  /**
   * Subtract the given scalar in place from each element of this matrix.
   *
   * @param value Scalar value
   * @return This matrix
   */
  public Matrix subi(double value) {
    physical().modifyAll(PrimitiveUnaryFunctions.sub(value));
    return this;
  }


  /**
   * Add the given matrix inplace to this matrix.
   *
   * @param other Matrix to add
   * @return This matrix
   */
  public Matrix subi(Matrix other) {
    physical().modifyMatching(PrimitiveFunction.SUBTRACT, other.data);
    return this;
  }

  /**
   * Apply elementwise power.
   *
   * @param exponent Exponent
   * @return Matrix with elementwise powered elements
   */
  public Matrix powElementwise(double exponent) {
    return create(data.operateOnAll(PrimitiveFunction.POW, exponent).get());
  }

  /**
   * Apply elementwise power.
   *
   * @param exponent Exponent
   * @return Matrix with elementwise powered elements
   */
  public Matrix powiElementwise(double exponent) {
    physical().modifyAll(PrimitiveUnaryFunctions.pow(exponent));
    return this;
  }

  protected PhysicalStore<Double> physical() {
    return (PhysicalStore<Double>) data;
  }

  /**
   * Apply the square root to all elements of this matrix.
   *
   * @return Matrix with elementwise square roots
   */
  public Matrix sqrt() {
    return create(data.operateOnAll(PrimitiveFunction.SQRT).get());
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
   * Shortcut for transposition.
   *
   * @return This matrix, transposed
   * @see Matrix#transpose()
   */
  public Matrix t() {
    return transpose();
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
    return MatrixFactory.fromRow(data.sliceRow(rowIdx));
  }

  /**
   * Get a column from this matrix.
   *
   * @param columnidx Column index
   * @return Column at the given index
   */
  public Matrix getColumn(int columnidx) {
    return MatrixFactory.fromColumn(data.sliceColumn(columnidx));
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
   * When this matrix is a 1 x 1 matrix, this method returns its value as
   * double.
   *
   * @return Matrix content
   */
  public double asDouble() {
    if (numRows() == 1 && numColumns() == 1) {
      return get(0, 0);
    }
    else {
      throw new MatrixAlgorithmsException("Method Matrix#asDouble is invalid " +
	"when number of rows != 1 or number of columns != 1.");
    }
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
    int numRows = numRows();
    int totalRows = numRows + other.numRows();
    PrimitiveDenseStore result = MatrixFactory.FACTORY.makeZero(totalRows, numColumns());
    for (int i = 0; i < totalRows; i++) {
      Access1D<Double> row;
      if (i < numRows) {
	row = data.sliceRow(i);
      }
      else {
	row = other.data.sliceRow(i - numRows);
      }
      result.fillRow(i, row);
    }
    return create(result);
  }

  /**
   * Concatenate this matrix with another along columns.
   *
   * @param other Other matrix which will be appended to this matrix
   * @return Concatenated matrices
   */
  public Matrix concatAlongColumns(Matrix other) {
    int numCols = numColumns();
    int totalCols = numCols + other.numColumns();
    PrimitiveDenseStore result = MatrixFactory.FACTORY.makeZero(numRows(), totalCols);
    for (int i = 0; i < totalCols; i++) {
      Access1D<Double> col;
      if (i < numCols) {
	col = data.sliceColumn(i);
      }
      else {
	col = other.data.sliceColumn(i - numCols);
      }
      result.fillColumn(i, col);
    }
    return create(result);
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

  /**
   * Modify each element by applying the given function to the element.
   *
   * @param body Function body
   */
  public Matrix modifyEach(Function<Double, Double> body) {
    return create(data.operateOnAll(new UnaryFunction<Double>() {
      @Override
      public double invoke(double arg) {
	return body.apply(arg);
      }

      @Override
      public Double invoke(Double arg) {
	return body.apply(arg);
      }
    }).get());
  }

  /**
   * Clip the matrix values with a lower and upper bound.
   *
   * @param lowerBound Lower bound threshold
   * @param upperBound Upper bound threshold
   */
  public Matrix clip(double lowerBound, double upperBound) {
    if (lowerBound > upperBound) {
      throw new MatrixAlgorithmsException("Invalid clipping values. Lower " +
	"bound must be below upper bound");
    }

    return modifyEach(value -> StrictMath.min(upperBound, StrictMath.max(lowerBound, value)));
  }

  /**
   * Clip matrix elements by lower bound.
   *
   * @param lowerBound Lower bound
   */
  public Matrix clipLower(double lowerBound) {
    return clip(lowerBound, Double.POSITIVE_INFINITY);
  }

  /**
   * Clip matrix elements by upper bound.
   *
   * @param upperBound Upper bound
   */
  public Matrix clipUpper(double upperBound) {
    return clip(Double.NEGATIVE_INFINITY, upperBound);
  }

  /**
   * Apply the signum function to each matrix element.
   */
  public Matrix sign() {
    return modifyEach(StrictMath::signum);
  }

  /**
   * Take the absolute of each element.
   */
  public Matrix abs() {
    return modifyEach(StrictMath::abs);
  }

  /**
   * Get the maximum value in that matrix.
   *
   * @return Max value
   */
  public double max() {
    return data
      .reduceColumns(Aggregator.MAXIMUM).get()
      .reduceRows(Aggregator.MAXIMUM).get()
      .get(0, 0);
  }

  /**
   * Get the median value in that matrix.
   *
   * @return Median value
   */
  public double median() {
    double[] rawData = data.toRawCopy1D();
    Arrays.sort(rawData);
    int size = rawData.length;
    if (size % 2 == 0) {
      return (rawData[size / 2] + rawData[size / 2 - 1]) / 2;
    }
    else {
      return rawData[size / 2];
    }
  }

  /**
   * Get indices that match the condition.
   *
   * @param condition Condition
   * @return List of indices that match the condition
   */
  public List<Integer> whereVector(Function<Double, Boolean> condition) {
    List<Integer> idxs = new ArrayList<>();
    data.loopAll((row, col) -> {
      if (condition.apply(get((int) row, (int) col))) {
	if (isRowVector()) {
	  idxs.add((int) col);
	}
	else if (isColumnVector()) {
	  idxs.add((int) row);
	}
	else {
	  throw new MatrixAlgorithmsException("whereVector is only applicable " +
	    "on either row or column vectors!");
	}
      }
    });

    return idxs;
  }

  /**
   * Get first 5 rows.
   *
   * @return First 5 rows
   */
  public Matrix head() {
    return head(5);
  }

  /**
   * Get first n rows.
   *
   * @return First n rows
   */
  public Matrix head(int n) {
    Matrix result = getRow(0);
    for (int i = 1; i < n; i++) {
      result = result.concat(getRow(i), 0);
    }
    return result;
  }

  /**
   * Get the diagonal vector of this matrix.
   *
   * @return Diagonal vector of this matrix
   */
  public Matrix diag() {
    Matrix res = MatrixFactory.zeros(Math.min(numRows(), numColumns()), 1);
    data.loopAll((row, col) -> {
      if (row == col) {
	res.set((int) row, 1, get((int) row, (int) col));
      }
    });
    return res;
  }

  /**
   * Get the mean over a certain Axis
   *
   * @return
   */
  public Matrix mean(int axis) {
    if (axis == -1) {
      return create(data
	.reduceColumns(Aggregator.SUM).get()
	.reduceRows(Aggregator.SUM).get()).div(numRows() * numColumns());
    }
    else if (axis == 0) {
      return create(data
	.reduceColumns(Aggregator.AVERAGE).get());
    }
    else if (axis == 1) {
      return create(data
	.reduceRows(Aggregator.AVERAGE).get());
    }
    else {
      throw new InvalidAxisException(axis);
    }
  }

  /**
   * Reduce the rows of this matrix to their norm 1 value.
   *
   * @return Vector of norm 1 values of each row
   */
  public Matrix reduceRowsL1() {
    return create(data.reduceRows(Aggregator.NORM1).get());
  }

  /**
   * Reduce the columns of this matrix to their norm 1 value.
   *
   * @return Vector of norm 1 values of each column
   */
  public Matrix reduceColumnsL1() {
    return create(data.reduceColumns(Aggregator.NORM1).get());
  }

  /**
   * Reduce the rows of this matrix to their norm 2 value.
   *
   * @return Vector of norm 2 values of each row
   */
  public Matrix reduceRowsL2() {
    return create(data.reduceRows(Aggregator.NORM2).get());
  }

  /**
   * Reduce the columns of this matrix to their norm 2 value.
   *
   * @return Vector of norm 2 values of each column
   */
  public Matrix reduceColumnsL2() {
    return create(data.reduceColumns(Aggregator.NORM2).get());
  }


  @Override
  public String toString() {
    return MatrixHelper.toString(this, false, ',', 3);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Matrix matrix = (Matrix) o;

    return data.equals(matrix.data, NumberContext.getMath(7));
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  /**
   * Create a string representation of the matrix's shape.
   *
   * @return Shape string
   */
  public String shapeString() {
    return "[" + numRows() + " x " + numColumns() + "]";
  }
}
