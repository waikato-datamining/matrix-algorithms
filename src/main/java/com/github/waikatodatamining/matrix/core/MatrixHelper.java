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
 * MatrixHelper.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import com.github.waikatodatamining.matrix.core.exceptions.InvalidShapeException;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Additional matrix operations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MatrixHelper {

  /**
   * Removes the specified column.
   *
   * @param data	the matrix to operate on
   * @param col		the column to remove
   * @return		the new column
   */
  public static Matrix deleteCol(Matrix data, int col) {
    return deleteCols(data, new int[]{col});
  }

  /**
   * Removes the specified columns.
   *
   * @param data	the matrix to operate on
   * @param cols	the column sto remove
   * @return		the new column
   */
  public static Matrix deleteCols(Matrix data, int[] cols) {
    Matrix	result;
    TIntList 	rows;
    TIntList	keep;
    int		i;
    int		j;

    keep = new TIntArrayList();
    for (i = 0; i < data.getColumnDimension(); i++)
      keep.add(i);
    for (int col: cols)
      keep.remove(col);

    rows = new TIntArrayList();
    for (j = 0; j < data.getRowDimension(); j++)
      rows.add(j);
    result = data.getMatrix(rows.toArray(), keep.toArray());

    return result;
  }

  /**
   * Returns the specified row as matrix.
   *
   * @param data	the matrix to get the row from
   * @param row		the row index (0-based)
   * @return		the row
   */
  public static Matrix rowAsVector(Matrix data, int row) {
    return data.getMatrix(row, row, 0, data.getColumnDimension() - 1);
  }

  /**
   * Calculates the mean for the specified column.
   *
   * @param data	the matrix to work on
   * @param col		the column to calculate the mean for
   * @return		the mean
   */
  public static double mean(Matrix data, int col) {
    double	result;
    int		i;

    result = 0.0;
    for (i = 0; i < data.getRowDimension(); i++)
      result += data.get(i, col) / data.getRowDimension();

    return result;
  }

  /**
   * Calculates the standard deviation (sample) for the specified column.
   *
   * @param data	the matrix to work on
   * @param col		the column to calculate the stdev for
   * @return		the stdev
   */
  public static double stdev(Matrix data, int col) {
    double	result;
    double	mean;
    int		i;

    mean   = mean(data, col);
    result = 0.0;
    for (i = 0; i < data.getRowDimension(); i++)
      result += Math.pow(data.get(i, col) - mean, 2);;
    result /= (data.getRowDimension() - 1);
    result = Math.sqrt(result);

    return result;
  }

  /**
   * returns the given column as a vector (actually a n x 1 matrix)
   *
   * @param m the matrix to work on
   * @param columnIndex the column to return
   * @return the column as n x 1 matrix
   */
  public static Matrix columnAsVector(Matrix m, int columnIndex) {
    Matrix result;
    int i;

    result = new Matrix(m.getRowDimension(), 1);

    for (i = 0; i < m.getRowDimension(); i++) {
      result.set(i, 0, m.get(i, columnIndex));
    }

    return result;
  }

  /**
   * stores the data from the (column) vector in the matrix at the specified
   * index
   *
   * @param v the vector to store in the matrix
   * @param m the receiving matrix
   * @param columnIndex the column to store the values in
   */
  public static void setColumnVector(Matrix v, Matrix m, int columnIndex) {
    m.setMatrix(0, m.getRowDimension() - 1, columnIndex, columnIndex, v);
  }

  /**
   * stores the data from the (row) vector in the matrix at the specified
   * index
   *
   * @param v the vector to store in the matrix
   * @param m the receiving matrix
   * @param rowIndex the row to store the values in
   */
  public static void setRowVector(Matrix v, Matrix m, int rowIndex) {
    m.setMatrix(rowIndex, rowIndex, 0, m.getColumnDimension() - 1, v);
  }

  /**
   * returns the (column) vector of the matrix at the specified index
   *
   * @param m the matrix to work on
   * @param columnIndex the column to get the values from
   * @return the column vector
   */
  public static Matrix getVector(Matrix m, int columnIndex) {
    return m.getMatrix(0, m.getRowDimension() - 1, columnIndex, columnIndex);
  }

  /**
   * determines the dominant eigenvector for the given matrix and returns it
   *
   * @param m the matrix to determine the dominant eigenvector for
   * @return the dominant eigenvector
   */
  public static Matrix getDominantEigenVector(Matrix m) {
    EigenvalueDecomposition eigendecomp;
    double[] eigenvalues;
    int index;
    Matrix result;

    eigendecomp = m.eig();
    eigenvalues = eigendecomp.getRealEigenvalues();
    index = Utils.maxIndex(eigenvalues);
    result = columnAsVector(eigendecomp.getV(), index);

    return result;
  }

  /**
   * normalizes the given vector (inplace)
   *
   * @param v the vector to normalize
   */
  public static void normalizeVector(Matrix v) {
    double sum;
    int i;

    // determine length
    sum = 0;
    for (i = 0; i < v.getRowDimension(); i++) {
      sum += v.get(i, 0) * v.get(i, 0);
    }
    sum = StrictMath.sqrt(sum);

    // normalize content
    for (i = 0; i < v.getRowDimension(); i++) {
      v.set(i, 0, v.get(i, 0) / sum);
    }
  }

  /**
   * Calculate the l2 vector norm.
   * This is faster than using {@link Matrix#norm2()} since it uses SVD
   * decomposition to get the largest eigenvalue.
   * @param v Input vector
   * @return L2 norm of the input vector
   */
  public static double l2VectorNorm(Matrix v){
    double sum = 0.0;
    int columns = v.getColumnDimension();
    int rows = v.getRowDimension();
    if (rows == 1){
      for (int col = 0; col < columns; col++) {
        double val = v.get(0,col);
        sum += val*val;
      }
    } else if(columns == 1){
      for (int row = 0; row < rows; row++) {
        double val = v.get(row, 0);
        sum += val*val;
      }
    } else {
      // Not a vector
      throw new InvalidShapeException("MatrixHelper.l2VectorNorm() can only " +
        "be applied on row or column vectors.");
    }

    return StrictMath.sqrt(sum);
  }

  /**
   * Compares the two matrices.
   *
   * @param m1		the first matrix
   * @param m2		the second matrix
   * @return		true if the same dimension and values, otherwise false
   */
  public static boolean equal(Matrix m1, Matrix m2) {
    return equal(m1, m2, 0.0);
  }

  /**
   * Compares the two matrices.
   *
   * @param m1		the first matrix
   * @param m2		the second matrix
   * @param epsilon	the minimal accepted difference between the cells
   * @return		true if the same dimension and values, otherwise false
   */
  public static boolean equal(Matrix m1, Matrix m2, double epsilon) {
    int		i;
    int		n;

    if (m1.getColumnDimension() != m2.getColumnDimension())
      return false;
    if (m1.getRowDimension() != m2.getRowDimension())
      return false;

    for (i = 0; i < m1.getRowDimension(); i++) {
      for (n = 0; n < m1.getColumnDimension(); n++) {
	if (Math.abs(m1.get(i, n) - m2.get(i, n)) > epsilon)
	  return false;
      }
    }

    return true;
  }

  /**
   * Merges the two matrices (must have same number of rows).
   *
   * @param left	the left matrix
   * @param right	the right matrix
   * @return		the merged matrix
   */
  public static Matrix merge(Matrix left, Matrix right) {
    Matrix	result;
    int		i;

    if (left.getRowDimension() != right.getRowDimension())
      throw new IllegalArgumentException("Matrix row dimension differs: " + left.getRowDimension() + " != " + right.getRowDimension());

    result = new Matrix(left.getRowDimension(), left.getColumnDimension() + right.getColumnDimension());
    result.setMatrix(0, left.getRowDimension() - 1, 0, left.getColumnDimension() - 1, left);
    for (i = 0; i < right.getColumnDimension(); i++)
      setColumnVector(columnAsVector(right, i), result, left.getColumnDimension() + i);

    return result;
  }

  /**
   * Reads the matrix from the given CSV file.
   *
   * @param filename	the file to read from
   * @param header 	true if the file contains a header (gets skipped)
   * @param separator	the column separator used
   * @return		the matrix
   */
  public static Matrix read(String filename, boolean header, char separator) throws Exception {
    Matrix 		result;
    List<String> lines;
    String[]		cells;
    String		sep;
    int			i;
    int			j;

    lines = Files.readAllLines(new File(filename).toPath());
    if (lines.size() == 0)
      throw new IllegalStateException("No rows in file: " + filename);

    if (header)
      lines.remove(0);
    if (lines.size() == 0)
      throw new IllegalStateException("No data rows in file: " + filename);

    sep    = "" + separator;
    cells  = lines.get(0).split(sep);
    result = new Matrix(lines.size(), cells.length);
    for (i = 0; i < lines.size(); i++) {
      cells = lines.get(i).split(sep);
      for (j = 0; j < cells.length && j < result.getColumnDimension(); j++) {
	try {
	  result.set(i, j, Double.parseDouble(cells[j]));
	}
	catch (Exception e) {
	  System.err.println("Failed to parse row=" + (header ? (i+1) : i) + " col=" + j + ": " + cells[j]);
	  e.printStackTrace();
	}
      }
    }

    return result;
  }

  /**
   * Turns the matrix into a list of strings.
   *
   * @param data	the matrix to output
   * @param header	whether to add a fake header
   * @param separator	the column separator to use
   * @param numDec 	the number of decimals after the decimal point, -1 for default
   * @return		the lines
   */
  protected static List<String> toLines(Matrix data, boolean header, char separator, int numDec) {
    List<String>  	result;
    StringBuilder	line;
    int			i;
    int			j;

    result = new ArrayList<>();
    if (header) {
      line = new StringBuilder();
      for (j = 0; j < data.getColumnDimension(); j++) {
	if (j > 0)
	  line.append(separator);
	line.append("col" + (j+1));
      }
      result.add(line.toString());
    }

    for (i = 0; i < data.getRowDimension(); i++) {
      line = new StringBuilder();
      for (j = 0; j < data.getColumnDimension(); j++) {
	if (j > 0)
	  line.append(separator);
	if (numDec == -1)
	  line.append(Double.toString(data.get(i, j)));
	else
	  line.append(Utils.doubleToStringFixed(data.get(i, j), numDec));
      }
      result.add(line.toString());
    }

    return result;
  }

  /**
   * Writes the matrix to the specified file.
   *
   * @param data	the matrix to output
   * @param filename	the file to write to
   * @param header	whether to add a fake header
   * @param separator	the column separator to use
   * @param numDec 	the number of decimals after the decimal point, -1 for default
   * @throws Exception	if failed to write
   */
  public static void write(Matrix data, String filename, boolean header, char separator, int numDec) throws Exception {
    Files.write(
      new File(filename).toPath(),
      toLines(data, header, separator, numDec),
      StandardOpenOption.WRITE,StandardOpenOption.CREATE);
  }

  /**
   * Turns the matrix into a string (with header, \t as separator and 6 decimals).
   *
   * @param data	the matrix to output
   * @return 		the matrix as string
   */
  public static String toString(Matrix data) {
    return toString(data, true, '\t', 6);
  }

  /**
   * Turns the matrix into a string.
   *
   * @param data	the matrix to output
   * @param header	whether to add a fake header
   * @param separator	the column separator to use
   * @param numDec 	the number of decimals after the decimal point, -1 for default
   * @return 		the matrix as string
   */
  public static String toString(Matrix data, boolean header, char separator, int numDec) {
    StringBuilder	result;
    List<String>	lines;

    result = new StringBuilder();
    lines  = toLines(data, header, separator, numDec);
    for (String line: lines) {
      if (result.length() > 0)
	result.append("\n");
      result.append(line);
    }

    return result.toString();
  }

  /**
   * Returns a "ROWS x COLS" dimension string.
   *
   * @param m		the matrix to get the description string for
   * @return		the dimensions
   */
  public static String dim(Matrix m) {
    return m.getRowDimension() + " x " + m.getColumnDimension();
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
      Matrix A = new Matrix(m, n);
      double[][] X = A.getArray();
      for (int i = 0; i < m; i++) {
          for (int j = 0; j < n; j++) {
              X[i][j] = rand.nextGaussian();
          }
      }
      return A;
  }
  /**
   * Generate matrix with random elements, sampled from a uniform distribution in (0, 1).
   *
   * @param m       Number of rows
   * @param n       Number of columns
   * @param seed    Seed for the random number generator
   * @return        An m-by-n matrix with uniformly distributed random elements
   */
  public static Matrix rand(int m, int n, long seed) {
      Random rand = new Random(seed);
      Matrix A = new Matrix(m, n);
      double[][] X = A.getArray();
      for (int i = 0; i < m; i++) {
          for (int j = 0; j < n; j++) {
              X[i][j] = rand.nextDouble();
          }
      }
      return A;
  }

  /**
   * Apply the signum function to a matrix inplace.
   *
   * @param mat Matrix so apply signum inplace
   */
  public static void sign(Matrix mat) {
    for (int i = 0; i < mat.getRowDimension(); i++) {
      for (int j = 0; j < mat.getColumnDimension(); j++) {
        double v = mat.get(i, j);
        double sign = StrictMath.signum(v);
        mat.set(i, j, sign);
      }
    }
  }
}
