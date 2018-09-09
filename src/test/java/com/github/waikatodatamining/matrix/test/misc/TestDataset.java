package com.github.waikatodatamining.matrix.test.misc;


import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import org.junit.jupiter.api.Assertions;

/**
 * Test datasets that represent a file in src/test/resources/com/github/waikatodatamining/matrix.
 *
 * @author Steven Lang
 */
public enum TestDataset {
  BOLTS("src/test/resources/com/github/waikatodatamining/matrix/data/bolts.csv"),
  BOLTS_RESPONSE("src/test/resources/com/github/waikatodatamining/matrix/data/bolts_response.csv");

  private String m_name;

  TestDataset(String name) {
    this.m_name = name;
  }

  @Override
  public String toString() {
    return m_name;
  }


  /**
   * Load a matrix from a given input path.
   *
   * @return Matrix stored in input path
   */
  public Matrix load() {
    try {
      return MatrixHelper.read(m_name, true, ',');
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("Failed to load dataset at path: " + m_name);
      return null;
    }
  }
}
