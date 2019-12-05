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
 * SavitzkyGolay.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecomposition;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import java.util.ArrayList;

/**
 * Performs Savitzky-Golay smoothing of the data in the matrix columns.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class SavitzkyGolay
  extends UnsupervisedMatrixAlgorithm {

  protected int m_PolynomialOrder = 2;

  protected int m_DerivativeOrder = 1;

  protected int m_NumPointsLeft = 3;

  protected int m_NumPointsRight = 3;

  protected Matrix m_Coefficients;

  public int getPolynomialOrder() { return m_PolynomialOrder; }

  public void setPolynomialOrder(int value) {
    if (value < 2)
      throw new IllegalArgumentException("Polynomial order must be at least 2");

    m_PolynomialOrder = value;
    reset();
  }

  public int getDerivativeOrder() { return m_DerivativeOrder; }

  public void setDerivativeOrder(int value) {
    if (value < 0)
      throw new IllegalArgumentException("Derivative order must be at least 0");

    m_DerivativeOrder = value;
    reset();
  }

  public int getNumPointsLeft() { return m_NumPointsLeft; }

  public void setNumPointsLeft(int value) {
    if (value < 0)
      throw new IllegalArgumentException("Number of left points must be at least 0");

    m_NumPointsLeft = value;
    reset();
  }

  public int getNumPointsRight() { return m_NumPointsRight; }

  public void setNumPointsRight(int value) {
    if (value < 0)
      throw new IllegalArgumentException("Number of right points must be at least 0");

    m_NumPointsRight = value;
    reset();
  }

  @Override
  public void doReset() {
    m_Coefficients = null;
  }

  @Override
  public void doConfigure(Matrix data) {
    if (m_Coefficients == null)
      m_Coefficients = MatrixFactory.fromRaw(new double[][] { determineCoefficients(m_NumPointsLeft,
	                                                                            m_NumPointsRight,
	                                                                            m_PolynomialOrder,
					                                            m_DerivativeOrder) });
  }

  @Override
  protected Matrix doTransform(Matrix data) {
    ArrayList<Matrix> smoothedColumns = new ArrayList<>();

    int windowWidth = m_Coefficients.numColumns();
    int numOutputColumns = data.numColumns() - windowWidth + 1;
    for (int i = 0; i < numOutputColumns; i++) {
      Matrix column = data.getSubMatrix(0, data.numRows(), i, i + windowWidth);
      column = column.scaleByRowVector(m_Coefficients.transpose());
      column = column.sum(1);
      smoothedColumns.add(column);
    }

    return MatrixHelper.multiConcat(1, smoothedColumns.toArray(new Matrix[0]));
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }

  /**
   * Determines the coefficients for the smoothing, with optional debugging
   * output.
   *
   * Copied from ADAMS.
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   *
   * @param numLeft	the number of points to the left
   * @param numRight	the number of points to the right
   * @param polyOrder	the polynomial order
   * @param derOrder	the derivative order
   * @return		the coefficients
   */
  public static double[] determineCoefficients(int numLeft, int numRight, int polyOrder, int derOrder) {
    double[]		result;
    RealMatrix          A;
    int			i;
    int			j;
    int			k;
    float		sum;
    RealMatrix		b;
    LUDecomposition     lu;
    RealMatrix		solution;

    result = new double[numLeft + numRight + 1];

    // no window?
    if (result.length == 1) {
      result[0] = 1.0;
      return result;
    }

    // Note: "^" = superscript, "." = subscript

    // {A^T*A}.ij = Sum[k:-nl..nr](k^(i+j))
    A = new Array2DRowRealMatrix(polyOrder + 1, polyOrder + 1);
    for (i = 0; i < A.getRowDimension(); i++) {
      for (j = 0; j < A.getColumnDimension(); j++) {
	sum = 0;
	for (k = -numLeft; k <= numRight; k++)
	  sum += Math.pow(k, i + j);
	A.setEntry(i, j, sum);
      }
    }

    // LU decomp for inverse matrix
    b = new Array2DRowRealMatrix(polyOrder + 1, 1);
    b.setEntry(derOrder, 0, 1.0);

    lu       = new LUDecompositionImpl(A);
    solution = lu.getSolver().solve(b);

    // coefficients: c.n = Sum[m:0..M]((A^T*A)^-1).0m * n^m with n=-nl..nr
    for (i = -numLeft; i <= numRight; i++) {
      sum = 0;
      for (j = 0; j <= polyOrder; j++)
	sum += solution.getEntry(j, 0) * Math.pow(i, j);
      result[i + numLeft] = sum;
    }

    return result;
  }
}
