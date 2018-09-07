package com.github.waikatodatamining.matrix.algorithm.ica;

import com.github.waikatodatamining.matrix.algorithm.AbstractAlgorithmTest;
import com.github.waikatodatamining.matrix.algorithm.ica.FastICA.Algorithm;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.Cube;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.Exponential;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.LogCosH;
import com.github.waikatodatamining.matrix.core.Matrix;

public class FastICATest extends AbstractAlgorithmTest<FastICA> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public FastICATest(String name) {
    super(name);
  }

  @Override
  protected Matrix process(Matrix data, FastICA scheme) {
    try {
      return scheme.transform(data);
    }
    catch (Exception e) {
      fail("Failed to transform data: " + stackTraceToString(e));
      return null;
    }
  }

  @Override
  protected String[] getRegressionInputFiles() {
    return new String[]{
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
      "bolts.csv",
    };
  }

  @Override
  protected FastICA[] getRegressionSetups() {
    FastICA[]	result;

    result    = new FastICA[5];
    result[0] = new FastICA();
    result[0].setAlgorithm(Algorithm.DEFLATION);
    result[0].setFun(new LogCosH());
    result[0].setWhiten(true);
    result[0].setNumComponents(2);

    result[1] = new FastICA();
    result[1].setAlgorithm(Algorithm.PARALLEL);
    result[1].setFun(new LogCosH());
    result[1].setWhiten(true);
    result[1].setNumComponents(2);

    result[2] = new FastICA();
    result[2].setAlgorithm(Algorithm.PARALLEL);
    result[2].setFun(new LogCosH());
    result[2].setWhiten(true);
    result[2].setNumComponents(2);

    result[2] = new FastICA();
    result[2].setAlgorithm(Algorithm.DEFLATION);
    result[2].setFun(new Exponential());
    result[2].setWhiten(true);
    result[2].setNumComponents(2);

    result[3] = new FastICA();
    result[3].setAlgorithm(Algorithm.DEFLATION);
    result[3].setFun(new Cube());
    result[3].setWhiten(true);
    result[3].setNumComponents(2);

    result[4] = new FastICA();
    result[4].setAlgorithm(Algorithm.DEFLATION);
    result[4].setFun(new LogCosH());
    result[4].setWhiten(false);
    result[4].setNumComponents(2);

    result[4] = new FastICA();
    result[4].setAlgorithm(Algorithm.PARALLEL);
    result[4].setFun(new LogCosH());
    result[4].setWhiten(false);
    result[4].setNumComponents(2);


    return result;
  }
}
