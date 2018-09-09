package com.github.waikatodatamining.matrix.test.regression;

import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Double value regression implementation.
 *
 * @author Steven Lang
 */
public class DoubleRegression extends AbstractRegression<Double> {

  /**
   * Constructor with regression path for reference file and actual result which
   * will be compared to the expected result.
   *
   * @param path   Path to reference file
   * @param actual Actual result (will be compared to expected result)
   */
  public DoubleRegression(String path, Double actual) {
    super(path, actual);
  }

  @Override
  protected void checkEquals(Double expected, Double actual) {
    Assertions.assertEquals(expected, actual, EPSILON);
  }


  @Override
  protected Double readExpected(String path) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(path));
    String line = br.readLine();
    br.close();
    return Double.parseDouble(line);
  }

  @Override
  protected void writeExpected(String path, Double expected) throws Exception {
    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
    bw.write(String.valueOf(expected));
    bw.close();
  }

  @Override
  protected String getFilenameExtension() {
    return "txt";
  }
}
