package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;

/**
 * Five Point First Derivate Savitzky Golay Filter.
 *
 * @author Steven Lang
 */
public class SavitzkyGolayFilter extends AbstractTransformation {

  private static final long serialVersionUID = 7783793644680234716L;


  /** Five Point Savitzky Golay Filter coefficients (first derivative) */
  protected static final double[] m_Coef = {2.0 / 10.0, 1.0 / 10.0, 0.0, -1.0 / 10.0, -2.0 / 10.0};

  @Override
  public void configure(Matrix data) {
    m_Configured = true;
  }

  @Override
  protected Matrix doTransform(Matrix data) {
    Matrix matExtended = extendMatrix(data);
    Matrix result = MatrixFactory.zerosLike(matExtended);

    // Smooth all rows
    for (int i = 2; i < matExtended.numRows() - 2; i++) {
      Matrix rowSmoothed = smoothRow(i, matExtended);
      result.setRow(i, rowSmoothed);
    }

    Matrix origSizedMatrix = shrinkMatrix(result);
    return origSizedMatrix;
  }

  /**
   * Shrink matrix to the original size. Removes first and last two rows.
   *
   * @param result Input matrix
   * @return Shrunk matrix
   */
  private Matrix shrinkMatrix(Matrix result) {
    return result.getSubMatrix(2, result.numRows() - 2, 0, result.numColumns());
  }

  /**
   * Extend the matrix by 2 rows in the beginning and 2 rows in the end
   * (copy first and last elements).
   *
   * @param data Input matrix
   * @return Extended matrix
   */
  protected Matrix extendMatrix(Matrix data) {
    // Extend the matrix by two rows at the begging and two rows at the end
    Matrix firstRow = data.getRow(0);
    Matrix lastRow = data.getRow(data.numRows() - 1);
    return firstRow
      .concatAlongRows(firstRow)
      .concatAlongRows(data)
      .concatAlongRows(lastRow)
      .concatAlongRows(lastRow);
  }

  @Override
  protected Matrix doInverseTransform(Matrix data) {
    throw new MatrixAlgorithmsException("Inverse transformation of Savitzky " +
      "Golay is not available.");
  }

  /**
   * Apply first five-point first gradient Savitzky Golay smoothing to the i-th
   * row of the given matrix.
   *
   * @param i      Row index
   * @param matrix Input matrix
   * @return Smoothed row
   */
  protected Matrix smoothRow(int i, Matrix matrix) {
    Matrix res = MatrixFactory.zeros(1, matrix.numColumns());

    int windowSize = m_Coef.length;
    for (int m = 0; m < windowSize; m++) {
      int coefIdx = (windowSize - 1) - m;
      int rowIdx = i - (m - 2);
      Matrix row = matrix.getRow(rowIdx);
      Matrix rowScaled = row.mul(m_Coef[coefIdx]);
      res = res.add(rowScaled);
    }

    return res;
  }
}
