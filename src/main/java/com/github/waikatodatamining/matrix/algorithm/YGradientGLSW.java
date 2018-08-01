package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.transformation.SavitzkyGolayFilter;

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
    if (x1.numRows() != x2.numRows())
      return "Predictors and response must have the same number of rows!";
    return null;
  }
}
