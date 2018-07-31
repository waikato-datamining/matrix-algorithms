package com.github.waikatodatamining.matrix.core;

import Jama.EigenvalueDecomposition;
import com.github.waikatodatamining.matrix.core.exceptions.InvalidShapeException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test to guarantee the same results for the transition between JAMA and ojAlgo
 * as Matrix base library.
 *
 * @author Steven Lang
 */
public class MatrixTest {

  private static final double PRECISION = 1e-7;

  protected Matrix a;

  protected Matrix b;

  protected Jama.Matrix ja;

  protected Jama.Matrix jb;

  @Before
  public void init() {
    //    double[][] dataA = {{1, 2}, {3, 4}, {5, 6}};
    //    double[][] dataB = {{10, 11}, {12, 13}, {14, 15}};
    double[][] dataA = MatrixFactory.randn(10, 20, 1).toRawCopy2D();
    double[][] dataB = MatrixFactory.randn(10, 20, 2).toRawCopy2D();


    a = MatrixFactory.fromRaw(dataA);
    b = MatrixFactory.fromRaw(dataB);
    ja = new Jama.Matrix(dataA).copy();
    jb = new Jama.Matrix(dataB).copy();

    assertMatrixEquals(ja, a);
    assertMatrixEquals(jb, b);
  }

  public static void assertMatrixEquals(Jama.Matrix jx, Matrix x) {
    assertEquals(x, MatrixFactory.fromRaw(jx.getArray()));
  }

  @Test
  public void getSubMatrix() {
    assertMatrixEquals(ja.getMatrix(0, 0, 1, 1), a.getSubMatrix(0, 1, 1, 2));
  }

  @Test
  public void getSubMatrix1() {
    int[] rows = {1};
    int[] cols = {0};
    assertMatrixEquals(ja.getMatrix(rows, cols), a.getSubMatrix(rows, cols));
  }

  @Test
  public void getEigenvectors() {
    Matrix aa = a.mul(a.transpose());
    Jama.Matrix aaj = ja.times(ja.transpose());
    Matrix eigVectors = aa.getEigenvectorsSortedAscending();
    Jama.Matrix jamaEigVectors = aaj.eig().getV();
    assertMatrixEquals(jamaEigVectors, eigVectors);
  }

  @Test
  public void getEigenvalues() {
    Matrix aa = a.mul(a.transpose());
    Jama.Matrix aaj = ja.times(ja.transpose());
    Matrix jamaEigVals = MatrixFactory.fromColumn(aaj.eig().getRealEigenvalues());
    Matrix eigVals = aa.getEigenvaluesSortedAscending();
    assertEquals(jamaEigVals, eigVals);
  }

  @Test
  public void svdU() {

  }

  @Test
  public void svdV() {
  }

  @Test
  public void mul() {
    assertMatrixEquals(ja.times(jb.transpose()), a.mul(b.transpose()));
    assertMatrixEquals(ja.times(ja.transpose()), a.mul(a.transpose()));
  }

  @Test
  public void vectorDot() {
    Jama.Matrix subJA = ja.getMatrix(0, ja.getRowDimension() - 1, 0, 0).transpose();
    Jama.Matrix subJB = jb.getMatrix(0, ja.getRowDimension() - 1, 0, 0).transpose();
    Matrix subA = a.getColumn(0).transpose();
    Matrix subB = b.getColumn(0).transpose();

    double expected = subA.vectorDot(subB);
    double actual = MatrixFactory.fromRaw(subJA.getArray()).vectorDot(MatrixFactory.fromRaw(subJB
      .getArray()));
    double actual2 = subJA.times(subJB.transpose()).get(0, 0);

    assertMatrixEquals(subJA, subA);
    assertMatrixEquals(subJB, subB);
    assertEquals(expected, actual, PRECISION);
    assertEquals(expected, actual2, PRECISION);
  }

  @Test
  public void mul1() {
    assertMatrixEquals(ja.times(5), a.mul(5));
  }

  @Test
  public void div() {
    assertMatrixEquals(ja.times(1.0 / 5.0), a.div(5.0));
  }

  @Test
  public void sub() {
    assertMatrixEquals(ja.minus(jb), a.sub(b));
  }

  @Test
  public void add() {
    assertMatrixEquals(ja.plus(jb), a.add(b));
  }

  @Test
  public void add1() {
    assertMatrixEquals(ja.plus(new Jama.Matrix(MatrixFactory.filled(ja.getRowDimension(), ja.getColumnDimension(), 5).toRawCopy2D())), a.add(5));
  }

  @Test
  public void sub1() {
    assertMatrixEquals(ja.minus(new Jama.Matrix(MatrixFactory.filled(ja.getRowDimension(), ja.getColumnDimension(), 5).toRawCopy2D())), a.sub(5));
  }

  @Test
  public void transpose() {
    assertMatrixEquals(ja.transpose(), a.transpose());
  }

  @Test
  public void numColumns() {
    assertEquals(a.numColumns(), ja.getColumnDimension());
  }

  @Test
  public void numRows() {
    assertEquals(a.numRows(), ja.getRowDimension());
  }

  @Test
  public void get() {
    assertEquals(a.get(0, 1), ja.get(0, 1), PRECISION);
    assertEquals(a.get(1, 1), ja.get(1, 1), PRECISION);
  }

  @Test
  public void set() {
    a.set(1, 1, 100.0);
    ja.set(1, 1, 100.0);
    assertMatrixEquals(ja, a);
  }


  @Test
  public void getRow() {
    Matrix row = a.getRow(0);
    Jama.Matrix rowJ = ja.getMatrix(0, 0, 0, ja.getColumnDimension() - 1);
    assertMatrixEquals(rowJ, row);
  }

  @Test
  public void getColumn() {
    Matrix col = a.getColumn(0);
    Jama.Matrix colJ = ja.getMatrix(0, ja.getRowDimension() - 1, 0, 0);
    assertMatrixEquals(colJ, col);
  }

  @Test
  public void inverse() {
    // Create square matrices

    double[][] d = {{1, 2}, {3, 4}};
    double[][] expectedInverse = {{-2, 1}, {3.0 / 2.0, -1.0 / 2.0}};
    double[][] identity = {{1, 0}, {0, 1}};

    Matrix mat = MatrixFactory.fromRaw(d);
    Matrix matInvExp = MatrixFactory.fromRaw(expectedInverse);
    Matrix matId = MatrixFactory.fromRaw(identity);

    Jama.Matrix matJama = new Jama.Matrix(mat.toRawCopy2D());

    // Invert matrices
    Jama.Matrix matJamaInvAct = matJama.inverse();
    Matrix matInvAct = mat.inverse();

    Jama.Matrix matJamaIdAct = matJama.times(matJamaInvAct);
    Matrix matIdAct = mat.mul(matInvAct);

    assertEquals(matInvExp, matInvAct);
    assertEquals(matId, matIdAct);
    assertMatrixEquals(matJamaIdAct, matIdAct);
    assertMatrixEquals(matJamaInvAct, matInvAct);
  }

  @Test
  public void copy() {
    assertMatrixEquals(ja.copy(), a.copy());
  }

  @Test
  public void asDouble() {
    assertEquals(MatrixFactory.filled(1, 1, 1.0).asDouble(), 1.0, PRECISION);
  }

  @Test
  public void toRawCopy1D() {
    double[][] data = {{1, 2, 3, 4}};
    double[] doubles1 = new Jama.Matrix(data).getRowPackedCopy();
    double[] doubles2 = MatrixFactory.fromRaw(data).toRawCopy1D();

    assertArrayEquals(doubles1, doubles2, PRECISION);
  }

  @Test
  public void toRawCopy2D() {
    assertMatrixEquals(new Jama.Matrix(a.toRawCopy2D()), MatrixFactory.fromRaw(ja.getArray()));
  }

  @Test
  public void concat() {
    double[][] data = {{1, 2}, {2, 3}, {3, 4}};
    double[][] row = {{4, 5}};
    double[][] dataWithRow = {{1, 2}, {2, 3}, {3, 4}, {4, 5}};
    double[][] column = {{3}, {4}, {5}};
    double[][] dataWithCol = {{1, 2, 3}, {2, 3, 4}, {3, 4, 5}};
    Matrix rowConcat = MatrixFactory.fromRaw(data).concat(MatrixFactory.fromRaw(row), 0);
    Matrix colConcat = MatrixFactory.fromRaw(data).concat(MatrixFactory.fromRaw(column), 1);

    assertEquals(rowConcat, MatrixFactory.fromRaw(dataWithRow));
    assertEquals(colConcat, MatrixFactory.fromRaw(dataWithCol));
  }


  @Test
  public void fromRow() {
    double[] row = {1, 2, 3};
    Matrix matrix = MatrixFactory.fromRow(row);
    assertEquals(1, matrix.numRows());
    assertEquals(3, matrix.numColumns());
  }

  @Test
  public void fromColumn() {
    double[] column = {1, 2, 3};
    Matrix matrix = MatrixFactory.fromColumn(column);
    assertEquals(1, matrix.numColumns());
    assertEquals(3, matrix.numRows());
  }

  @Test
  public void identity() {
    double[][] data = {{1, 0}, {0, 1}, {0, 0}};
    double[][] data2 = {{1, 0, 0}, {0, 1, 0}};
    Matrix id = MatrixFactory.eye(3, 2);
    Matrix id2 = MatrixFactory.eye(2, 3);

    assertEquals(MatrixFactory.fromRaw(data), id);
    assertEquals(MatrixFactory.fromRaw(data2), id2);
  }

  @Test
  public void identity1() {
    double[][] data2 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    Matrix id2 = MatrixFactory.eye(3, 3);

    assertEquals(MatrixFactory.fromRaw(data2), id2);
  }

  @Test
  public void norm2() {
    double oldL2Norm = l2VectorNorm(ja.getMatrix(0, ja.getRowDimension() - 1, 0, 0));
    double newL2Norm = a.getColumn(0).norm2();

    assertEquals(oldL2Norm, newL2Norm, PRECISION);
  }

  /**
   * Calculate the l2 vector norm.
   * This is faster than using {@link Matrix#norm2()} since it uses SVD
   * decomposition to get the largest eigenvalue.
   *
   * @param v Input vector
   * @return L2 norm of the input vector
   */
  public static double l2VectorNorm(Jama.Matrix v) {
    double sum = 0.0;
    int columns = v.getColumnDimension();
    int rows = v.getRowDimension();
    if (rows == 1) {
      for (int col = 0; col < columns; col++) {
	double val = v.get(0, col);
	sum += val * val;
      }
    }
    else if (columns == 1) {
      for (int row = 0; row < rows; row++) {
	double val = v.get(row, 0);
	sum += val * val;
      }
    }
    else {
      // Not a vector
      throw new InvalidShapeException("MatrixHelper.l2VectorNorm() can only " +
	"be applied on row or column vectors.");
    }

    return StrictMath.sqrt(sum);
  }

  @Test
  public void setRow() {
    double[][] data = new double[][]{{1, 2}, {3, 4}};
    Matrix mat = MatrixFactory.fromRaw(data);
    Matrix row = MatrixFactory.fromRaw(new double[][]{{10, 10}});
    mat.setRow(0, row);
    Matrix rowAct = mat.getRow(0);

    assertEquals(row, rowAct);
  }

  @Test
  public void setColumn() {
    double[][] data = new double[][]{{1, 2}, {3, 4}};
    Matrix mat = MatrixFactory.fromRaw(data);
    Matrix col = MatrixFactory.fromRaw(new double[][]{{10}, {10}});
    mat.setColumn(0, col);
    Matrix columnAct = mat.getColumn(0);

    assertEquals(col, columnAct);
  }

  @Test
  public void testEigenvectors() {
    Matrix rangeMat = MatrixHelper.range(5, 5, 10);
    Jama.Matrix rangeMatJama = new Jama.Matrix(rangeMat.toRawCopy2D());

    Matrix dominantEigenVector = MatrixHelper.getDominantEigenVector(rangeMat);
    Jama.Matrix dominantEigenVectorJama = getDominantEigenVectorJama(rangeMatJama);

    assertMatrixEquals(dominantEigenVectorJama, dominantEigenVector);
  }

  /**
   * determines the dominant eigenvector for the given matrix and returns it
   *
   * @param m the matrix to determine the dominant eigenvector for
   * @return the dominant eigenvector
   */
  public static Jama.Matrix getDominantEigenVectorJama(Jama.Matrix m) {
    EigenvalueDecomposition eigendecomp;
    double[] eigenvalues;
    int index;
    Jama.Matrix result;

    eigendecomp = m.eig();
    eigenvalues = eigendecomp.getRealEigenvalues();
    index = Utils.maxIndex(eigenvalues);
    result = columnAsVector(eigendecomp.getV(), index);

    return result;
  }

  /**
   * returns the given column as a vector (actually a n x 1 matrix)
   *
   * @param m           the matrix to work on
   * @param columnIndex the column to return
   * @return the column as n x 1 matrix
   */
  public static Jama.Matrix columnAsVector(Jama.Matrix m, int columnIndex) {
    Jama.Matrix result;
    int i;

    result = new Jama.Matrix(m.getRowDimension(), 1);

    for (i = 0; i < m.getRowDimension(); i++) {
      result.set(i, 0, m.get(i, columnIndex));
    }

    return result;
  }
}
