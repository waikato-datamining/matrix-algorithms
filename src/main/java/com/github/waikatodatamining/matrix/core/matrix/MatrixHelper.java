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

package com.github.waikatodatamining.matrix.core.matrix;

import com.github.waikatodatamining.matrix.core.Utils;
import com.github.waikatodatamining.matrix.core.exceptions.InvalidShapeException;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.ojalgo.matrix.store.PhysicalStore;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    for (i = 0; i < data.numColumns(); i++)
      keep.add(i);
    for (int col: cols)
      keep.remove(col);

    rows = new TIntArrayList();
    for (j = 0; j < data.numRows(); j++)
      rows.add(j);
    result = data.getSubMatrix(rows.toArray(), keep.toArray());

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
    return data.getSubMatrix(row, row + 1, 0, data.numColumns());
  }

  /**
   * Calculates the mean for the specified column.
   *
   * @param data	the matrix to work on
   * @param col		the column to calculate the mean for
   * @return		the mean
   */
  public static double mean(Matrix data, int col) {
    return mean(data, col,true);
  }

  /**
   * Calculates the mean for the specified row or column.
   *
   * @param data	the matrix to work on
   * @param index	the row or column to calculate the mean for
   * @param column      whether to calculate a row or column mean
   * @return		the mean
   */
  public static double mean(Matrix data, int index, boolean column) {
    double	result;
    int		i;

    result = 0.0;
    if (column)
      for (i = 0; i < data.numRows(); i++)
        result += data.get(i, index) / data.numRows();
    else
      for (i = 0; i < data.numColumns(); i++)
        result += data.get(index, i) / data.numColumns();

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
    return stdev(data, col, true);
  }

  /**
   * Calculates the standard deviation (sample) for the specified row
   * or column.
   *
   * @param data	the matrix to work on
   * @param index       the row or column to calculate the stdev for
   * @param column      whether to calculate a row or column stdev
   * @return		the stdev
   */
  public static double stdev(Matrix data, int index, boolean column) {
    double	result;
    double	mean;
    int		i;

    mean   = mean(data, index, column);
    result = 0.0;
    if (column) {
      for (i = 0; i < data.numRows(); i++)
        result += Math.pow(data.get(i, index) - mean, 2);
      result /= (data.numRows() - 1);
    } else {
      for (i = 0; i < data.numColumns(); i++)
        result += Math.pow(data.get(index, i) - mean, 2);
      result /= (data.numColumns() - 1);
    }
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

    result = MatrixFactory.zeros(m.numRows(), 1);

    for (i = 0; i < m.numRows(); i++) {
      result.set(i, 0, m.get(i, columnIndex));
    }

    return result;
  }


  /**
   * Get the euclidean distance matrix between the two matrices, that is
   * element (i,j) in the result is the l2-distance of X_i and Y_j.
   *
   * @param X       First matrix
   * @param Y       Second matrix
   * @param squared Whether the result shall be squared
   * @return Euclidean distance matrix
   */
  public static Matrix euclideanDistance(Matrix X, Matrix Y, boolean squared) {
    Matrix XX = rowNorms(X, true);
    Matrix YY = rowNorms(Y, true);

    Matrix distances = X.mul(Y.transpose());
    distances = distances.mul(-2);
    distances = distances.add(XX);
    distances = distances.add(YY);

    // Ensure i==j is set to zero (may not be the case due to floating point
    // errors
    if (X.equals(Y)) {
      ((PhysicalStore<Double>) distances.data).fillDiagonal(0.0);
    }

    // Skip square root if the squared distances are necessary anyway
    if (squared) {
      return distances;
    }
    else {
      return distances.sqrt();
    }
  }

  public static Matrix rowNorms(Matrix X, boolean squared){
    // TODO: Implement efficient elementwise multiplication.
    return null;
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

    if (m1.numColumns() != m2.numColumns())
      return false;
    if (m1.numRows() != m2.numRows())
      return false;

    for (i = 0; i < m1.numRows(); i++) {
      for (n = 0; n < m1.numColumns(); n++) {
	if (Math.abs(m1.get(i, n) - m2.get(i, n)) > epsilon)
	  return false;
      }
    }

    return true;
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
    result = MatrixFactory.zeros(lines.size(), cells.length);
    for (i = 0; i < lines.size(); i++) {
      cells = lines.get(i).split(sep);
      for (j = 0; j < cells.length && j < result.numColumns(); j++) {
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
    return  toLines(data, header, separator, numDec, false);
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
  protected static List<String> toLines(Matrix data, boolean header, char separator, int numDec, boolean scientific) {
    List<String>  	result;
    StringBuilder	line;
    int			i;
    int			j;

    result = new ArrayList<>();
    if (header) {
      line = new StringBuilder();
      for (j = 0; j < data.numColumns(); j++) {
	if (j > 0)
	  line.append(separator);
	line.append("col").append(j + 1);
      }
      result.add(line.toString());
    }

    dataToString(data, separator, numDec, result, scientific);

    return result;
  }

  private static void dataToString(Matrix data, char separator, int numDec, List<String> result, boolean scientific) {
    int i;
    StringBuilder line;
    int j;
    String numDecHashs;
    DecimalFormat formatter;

    char[] repeat = new char[numDec];
    Arrays.fill(repeat, '#');
    numDecHashs = new String(repeat);
    formatter = new DecimalFormat("0." + numDecHashs + "E0");

    for (i = 0; i < data.numRows(); i++) {
      line = new StringBuilder();
      for (j = 0; j < data.numColumns(); j++) {
	if (j > 0)
	  line.append(separator);

        if (scientific) {
          line.append(formatter.format(data.get(i, j)));
        }
        else if (numDec == -1) {
          line.append(data.get(i, j));
        }
        else {
          line.append(Utils.doubleToStringFixed(data.get(i, j), numDec));
        }
      }
      result.add(line.toString());
    }
  }

  /**
   * Writes the matrix to the specified file.
   *
   * @param data	the matrix to output
   * @param filename	the file to write to
   * @param header	whether to add a fake header
   * @param separator	the column separator to use
   * @param numDec 	the number of decimals after the decimal point, -1 for default
   * @param scientific  whether to enforce scientific mode on all values
   * @throws Exception	if failed to write
   */
  public static void write(Matrix data, String filename, boolean header, char separator, int numDec, boolean scientific) throws Exception {
    Files.write(
      new File(filename).toPath(),
      toLines(data, header, separator, numDec, scientific),
      StandardOpenOption.WRITE,StandardOpenOption.CREATE);
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
      toLines(data, header, separator, numDec, false),
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
    return m.numRows() + " x " + m.numColumns();
  }

  /**
   * Static method for matrix inverse. Makes the following eq. more clear:
   * B=W(P'W)^(-1)q
   * Without static method: B = W.mul(P.t().mul(W).inv()).mul(q)
   * With static method:    B = W.mul(inv(P.t().mul(W))).mul(q)
   *
   * @param x Input matrix
   * @return Inverse of input matrix
   */
  public static Matrix inv(Matrix x) {
    return x.inverse();
  }

  /**
   * Throw invalid shape exception
   * @param m1
   * @param m2
   * @throws InvalidShapeException
   */
  public static void throwInvalidShapes(Matrix m1, Matrix m2)throws InvalidShapeException {
    throw new InvalidShapeException("Invalid matrix multiplication. Shapes " +
      m1.shapeString() + " and " + m2.shapeString() + " do not match.");
  }

  /**
   * Concatenates several matrices together at once, in the order given.
   * @param axis      The axis to concatenate along.
   * @param matrices  The matrices to concatenate.
   * @return          The resulting concatenated matrix.
   */
  public static Matrix multiConcat(int axis, Matrix... matrices) {
    if (matrices == null || matrices.length == 0)
      return null;
    else if (matrices.length == 1)
      return matrices[0].copy();

    Matrix result = matrices[0];

    for (int i = 1; i < matrices.length; i++)
      result = result.concat(matrices[i], axis);

    return result;
  }

  /**
   * Calculates the covariance matrix between 2 matrices,
   * the rows of which represent the columns of the first
   * matrix and the columns of which represent the columns
   * of the second matrix.
   *
   * @param a   The first matrix.
   * @param b   The second matrix.
   * @return    The covariance matrix.
   */
  public static Matrix covariance(Matrix a, Matrix b) {
    // Initialise the result matrix
    Matrix covariance = MatrixFactory.zeros(a.numColumns(), b.numColumns());

    // Calculate the covariance between each pair of columns in the input matrices
    for (int i = 0; i < a.numColumns(); i++) {
      for (int j = 0; j < b.numColumns(); j++) {
        double cov = 0;
        for (int n = 0; n < a.numRows(); n++)
          cov += a.get(n, i) * b.get(n, j);

        cov /= a.numRows() - 1;
        covariance.set(i, j, cov);
      }
    }

    return covariance;
  }

  /**
   * Calculates the covariance between pairs of columns in a
   * single matrix.
   *
   * @param a   The matrix.
   * @return    The covariance matrix.
   */
  public static Matrix covariance(Matrix a) {
    return covariance(a, a);
  }
}
