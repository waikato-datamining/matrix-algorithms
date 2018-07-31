package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.transformation.Center;

import java.util.Comparator;
import java.util.stream.IntStream;

import static com.github.waikatodatamining.matrix.core.MatrixHelper.inv;

/**
 * Y-Gradient Generalized Least Squares Weighting.
 * <p>
 * See also: <a href="http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#GLS_Weighting_and_EPO">GLS Weighting</a>
 * * <p>
 * <p>
 * This implementation is similar to {@link GLSW} but is based on the Y block
 * instead of a second set of X samples.
 *
 * @author Steven Lang
 */
public class YGradientGLSW extends GLSW {


  /** Five Point Savitzky Golay Filter coefficients (first derivative)*/
  protected static final double[] m_Coef = {2.0 / 10.0, 1.0 / 10.0, 0.0, -1.0 / 10.0, -2.0 / 10.0};

  private static final long serialVersionUID = 4080767826836437539L;

  /** Savitzky Golay Filter constant h */
  protected double m_H;

  @Override
  protected void initialize() {
    super.initialize();
    m_H = 0.1;
  }

  private Matrix applyFivePointSavitzkyGolayFilter(Matrix matrix) {
    // Extend the matrix by two rows at the begging and two rows at the end
    Matrix firstRow = matrix.getRow(0);
    Matrix lastRow = matrix.getRow(matrix.numRows() - 1);
    Matrix matExtended = firstRow
      .concatAlongRows(firstRow)
      .concatAlongRows(matrix)
      .concatAlongRows(lastRow)
      .concatAlongRows(lastRow);

    Matrix result = MatrixFactory.zerosLike(matExtended);

    for (int i = 2; i < matExtended.numRows() - 2; i++) {
      Matrix rowSmoothed = smoothSavitzkyGolay(i, matExtended);
      result.setRow(i, rowSmoothed);
    }

    Matrix origSizedMatrix = result.getSubMatrix(2, result.numRows() - 2, 0, matrix.numColumns());
    return origSizedMatrix;
  }

  protected Matrix smoothSavitzkyGolay(int i, Matrix matrix) {
    Matrix res = MatrixFactory.zeros(1, matrix.numColumns());

    int windowSize = m_Coef.length;
    for (int m = 0; m < windowSize; m++) {
      int coefIdx = (windowSize - 1) - m;
      int rowIdx = i - (m - 2);
      Matrix row = matrix.getRow(rowIdx);
      Matrix rowScaled = row.mul(m_Coef[coefIdx]);
      res.addi(rowScaled);
    }

    return res;
  }

  @Override
  protected Matrix getCovarianceMatrix(Matrix X, Matrix y) {
    double[] yVals = y.toRawCopy1D();
    int[] sortedIncreasingRowIndices = IntStream
      .range(0, yVals.length)
      .boxed()
      .sorted(Comparator.comparingDouble(o -> yVals[o]))
      .mapToInt(i -> i)
      .toArray();

    // Sort increasing
    int[] allCols = IntStream.range(0, X.numColumns()).toArray();
    Matrix Xsorted = X.getSubMatrix(sortedIncreasingRowIndices, allCols);
    Matrix ysorted = y.getSubMatrix(sortedIncreasingRowIndices, new int[]{0});


    // Apply 5-Point first derivative Savitzky–Golay filter
    Matrix Xsmoothed = applyFivePointSavitzkyGolayFilter(Xsorted);
    Matrix ysmoothed = applyFivePointSavitzkyGolayFilter(ysorted);

    double ysmoothedMean = ysmoothed.mean(-1).asDouble();
    double syd = ysmoothed.sub(ysmoothedMean).powElementwise(2).sum(-1).div(ysmoothed.numRows() - 1).asDouble();

    // Reweighting matrix
    Matrix W = MatrixFactory.zeros(y.numRows(), y.numRows());
    for (int i = 0; i < ysmoothed.numRows(); i++) {
      double ydi = ysmoothed.get(i, 0);
      W.set(i, i, Math.pow(2, -1 * ydi / syd));
    }

    // Covariance Matrix
    Matrix C = Xsmoothed.t().mul(W.mul(W)).mul(Xsmoothed);
    return C;
  }

  /**
   * Hook method for checking the data before training.
   *
   * @param x1 first sample set
   * @param x2 second sample set
   * @return null if successful, otherwise error message
   */
  protected String check(Matrix x1, Matrix x2) {
    if (x1 == null)
      return "No x1 matrix provided!";
    if (x2 == null)
      return "No x2 matrix provided!";
    return null;
  }
}
