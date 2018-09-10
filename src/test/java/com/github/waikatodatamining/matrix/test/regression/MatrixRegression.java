package com.github.waikatodatamining.matrix.test.regression;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.core.Tuple;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Matrix regression implementation.
 *
 * @author Steven Lang
 */
public class MatrixRegression extends AbstractRegression<Matrix> {

  public MatrixRegression(String path, Matrix actual) {
    super(path, actual);
  }

  @Override
  protected void checkEquals(Matrix expected, Matrix actual) {
    // Check for shape
    if (expected.numColumns() != actual.numColumns()
      || expected.numRows() != actual.numRows()){
      fail("Shapes of the expected and actual matrices do not match.\n" +
        "Expected shape: " + expected.shapeString() + "\n" +
        "Actual shape: " + actual.shapeString());
    }

    // Check for values
    Matrix diff = expected.sub(actual).abs();
    List<Tuple<Integer, Integer>> which = diff.which(v -> v > EPSILON);
    if (!which.isEmpty()) {
      fail("Regression " + getPath() + " failed. \nAbsolute differences: \n" + indicesToString(which, diff));
    }
  }

  @Override
  protected Matrix readExpected(String path) throws Exception {
    return MatrixHelper.read(path, false, ',');
  }

  @Override
  protected void writeExpected(String path, Matrix expected) throws Exception {
    MatrixHelper.write(expected, path, false, ',', 100, true);
  }

  @Override
  protected String getFilenameExtension() {
    return "csv";
  }

  private String indicesToString(List<Tuple<Integer, Integer>> which, Matrix diff) {
    StringBuilder sb = new StringBuilder();
    sb.append("(<row>, <column>): <absolute difference>\n");
    for (Tuple<Integer, Integer> tuple : which) {
      int i = tuple.getFirst();
      int j = tuple.getSecond();
      double value = diff.get(i, j);
      sb.append("(").append(i).append(",").append(j).append("): ").append(value).append("\n");
    }
    return sb.toString();
  }
}
