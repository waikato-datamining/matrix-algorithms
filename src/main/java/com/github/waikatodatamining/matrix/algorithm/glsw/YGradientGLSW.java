package com.github.waikatodatamining.matrix.algorithm.glsw;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;
import com.github.waikatodatamining.matrix.transformation.AbstractTransformation;

import java.util.Comparator;
import java.util.stream.IntStream;


/**
 * Y-Gradient Generalized Least Squares Weighting.
 * <p>
 * See also: <a href="http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#GLS_Weighting_and_EPO">GLS Weighting</a>
 * * <p>
 * <p>
 * This implementation is similar to {@link GLSW} but is based on the Y block
 * instead of a second set of X samples.
 * <p>
 * Parameters:
 * - alpha: Defines how strongly GLSW downweights interferences
 *
 * @author Steven Lang
 */
public class YGradientGLSW extends GLSW {

  private static final long serialVersionUID = 4080767826836437539L;

  @Override
  protected void initialize() {
    super.initialize();
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

    SavitzkyGolayFilter savGolay = new SavitzkyGolayFilter();

    // Apply 5-Point first derivative Savitzky–Golay filter
    Matrix Xsmoothed = savGolay.transform(Xsorted);
    Matrix ysmoothed = savGolay.transform(ysorted);

    double ysmoothedMean = ysmoothed.mean(-1).asDouble();
    double syd = ysmoothed.sub(ysmoothedMean).powElementwise(2).sum(-1).div(ysmoothed.numRows() - 1).sqrt().asDouble();

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
    if (x1.numRows() != x2.numRows())
      return "Predictors and response must have the same number of rows!";
    return null;
  }

  /**
   * Five Point First Derivate Savitzky Golay Filter.
   *
   * @author Steven Lang
   */
  public static class SavitzkyGolayFilter extends AbstractTransformation {

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
}
