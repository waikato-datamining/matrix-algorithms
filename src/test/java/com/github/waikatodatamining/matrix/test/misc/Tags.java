package com.github.waikatodatamining.matrix.test.misc;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;

/**
 * Tags that should be used when calling
 * {@link AbstractRegressionTest#addRegression(String, Double)} or
 * {@link AbstractRegressionTest#addRegression(String, Matrix)}.
 *
 * @author Steven Lang
 */
public class Tags {

  public static final String TRANSFORM = "transform";

  public static final String INVERSE_TRANSFORM = "inverse-transform";

  public static final String LOADINGS = "loadings";

  public static final String SCORES = "scores";

  public static final String PREDICTIONS = "predictions";

  public static final String PROJECTION = "projection";

  public static final String MATRIX = "matrix";

  public static final String SUPERVISED = "supervised";

  public static final String SEMISUPERVISED = "semisupervised";

  public static final String UNSUPERVISED = "unsupervised";
}
