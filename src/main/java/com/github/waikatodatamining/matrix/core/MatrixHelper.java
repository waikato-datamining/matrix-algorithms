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

import Jama.Matrix;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
}
