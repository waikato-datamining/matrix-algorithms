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
public class YGradientGLSW extends AbstractSingleResponsePLS {


  /** Five Point Savitzky Golay Filter coefficients */
  protected static final double[] m_Coef = {2.0 / 10.0, 1.0 / 10.0, 0.0, -1.0 / 10.0, -2.0 / 10.0};

  /** Savitzky Golay Filter constant h */
  protected double m_H;

  @Override
  protected void initialize() {
    super.initialize();
    m_H = 0.1;
  }

  @Override
  protected void reset() {
    super.reset();
  }

  @Override
  public String[] getMatrixNames() {
    return new String[0];
  }

  @Override
  public Matrix getMatrix(String name) {
    return null;
  }

  @Override
  public boolean hasLoadings() {
    return false;
  }

  @Override
  public Matrix getLoadings() {
    return null;
  }

  @Override
  public boolean canPredict() {
    return false;
  }

  @Override
  protected Matrix doTransform(Matrix predictors) throws Exception {
    return null;
  }

  @Override
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    // Sort predictors and response in increasing Y value
    Matrix X = predictors;
    Matrix y = response;


    double[] yVals = y.toRawCopy1D();
    int[] sortedRowIndices = IntStream
      .range(0, yVals.length)
      .boxed()
      .sorted(Comparator.comparingDouble(o -> yVals[o]))
      .mapToInt(i -> i)
      .toArray();

    // Increasing/Decreasing??????
    int[] allCols = IntStream.range(0, X.numColumns()).toArray();
    Matrix Xsorted = X.getSubMatrix(sortedRowIndices, allCols);
    Matrix ysorted = y.getSubMatrix(sortedRowIndices, new int[]{0});


    // Apply 5-Point Savitzky–Golay filter
    Matrix Xsmoothed = applyFivePointSavitzkyGolayFilter(Xsorted);
    Matrix ysmoothed = applyFivePointSavitzkyGolayFilter(ysorted);

    System.out.println("ysmoothed = \n" + ysmoothed);

    return null;
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
  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {
    return null;
  }
}
