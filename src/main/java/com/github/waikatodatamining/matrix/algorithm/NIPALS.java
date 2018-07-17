package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import com.github.waikatodatamining.matrix.transformation.Standardize;

/**
 * Nonlinear Iterative Partial Least Squares
 *
 * Implementation oriented at scikit-learn's NIPALS implementation:
 * <a href="https://github.com/scikit-learn/scikit-learn/blob/ed5e127b/sklearn/cross_decomposition/pls_.py#L455">Github scikit-learn NIPALS</a>
 *
 * @author Steven Lang
 */
public class NIPALS extends AbstractMultiResponsePLS {

  private static final long serialVersionUID = -2760078672082710402L;

  /** Scores on X */
  protected Matrix m_XScores;

  /** Scores on Y */
  protected Matrix m_YScores;

  /** Loadings on X */
  protected Matrix m_XLoadings;

  /** Loadings on Y */
  protected Matrix m_YLoadings;

  /** Weights on X */
  protected Matrix m_XWeights;

  /** Weights on Y */
  protected Matrix m_YWeights;

  /** Projection of X into latent space */
  protected Matrix m_XRotations;

  /** Projection of Y into latent space */
  protected Matrix m_YRotations;

  /** Training points */
  protected Matrix m_X;

  /** Regression coefficients */
  protected Matrix m_Coef;

  /** Inner NIPALS loop improvement tolerance */
  protected double m_Tol;

  /** Inner NIPALS loop maximum number of iterations */
  protected int m_MaxIter;

  /** Flag to normalize Y weights */
  protected boolean m_NormYWeights;

  /** Standardize X transformation */
  protected Standardize m_StandardizeX;

  /** Standardize Y transformation */
  protected Standardize m_StandardizeY;

  /** X and Y deflation Mode */
  protected DeflationMode m_deflationMode;

  @Override
  protected void initialize() {
    super.initialize();
    setTol(1e-6);
    setMaxIter(500);
    setNormYWeights(false);
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();

    setDeflationMode(DeflationMode.REGRESSION);
  }

  public boolean isNormYWeights() {
    return m_NormYWeights;
  }

  public void setNormYWeights(boolean normYWeights) {
    m_NormYWeights = normYWeights;
  }

  public int getMaxIter() {
    return m_MaxIter;
  }

  public void setMaxIter(int maxIter) {
    this.m_MaxIter = maxIter;
  }

  public double getTol() {
    return m_Tol;
  }

  public void setTol(double tol) {
    this.m_Tol = tol;
  }

  @Override
  protected int getMinColumnsResponse() {
    return 1;
  }

  @Override
  protected int getMaxColumnsResponse() {
    return -1;
  }

  public DeflationMode getDeflationMode() {
    return m_deflationMode;
  }

  public void setDeflationMode(DeflationMode deflationMode) {
    m_deflationMode = deflationMode;
  }

  @Override
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix X, Y, xkScore, ykScore, xkLoading, ykLoading, xkWeight, ykWeight;

    getLogger();

    // Init
    X = predictors;
    X = m_StandardizeX.transform(X);
    Y = response;
    Y = m_StandardizeY.transform(Y);

    // Dimensions
    int numRows = X.numRows();
    int numFeatures = X.numColumns();
    int numClasses = Y.numColumns();
    int numComponents = getNumComponents();

    // Init matrices
    m_XScores = MatrixFactory.zeros(numRows, numComponents); // T
    m_YScores = MatrixFactory.zeros(numRows, numComponents); // U

    m_XWeights = MatrixFactory.zeros(numFeatures, numComponents); // W
    m_YWeights = MatrixFactory.zeros(numClasses, numComponents); // C

    m_XLoadings = MatrixFactory.zeros(numFeatures, numComponents); // P
    m_YLoadings = MatrixFactory.zeros(numClasses, numComponents); // Q

    ykLoading = MatrixFactory.zeros(numClasses, 1);

    double eps = 1e-10;
    for (int k = 0; k < numComponents; k++) {
      NipalsLoopResult res = nipalsLoop(X, Y);
      xkWeight = res.xWeights;
      ykWeight = res.yWeights;

      // Calculate latent X and Y scores
      xkScore = X.mul(xkWeight);
      ykScore = Y.mul(ykWeight).div(ykWeight.norm2squared());


      if (xkScore.norm2squared() < eps) {
	m_Logger.warning("X scores are null at component " + k);
	break;
      }

      // Deflate X
      xkLoading = X.t().mul(xkScore).div(xkScore.norm2squared());
      X.subi(xkScore.mul(xkLoading.t()));

      // Deflate Y
      switch (m_deflationMode) {
	case CANONICAL:
	  ykLoading = Y.t().mul(ykScore).div(ykScore.norm2squared());
	  Y.subi(ykScore.mul(ykLoading.t()));
	  break;
	case REGRESSION:
	  ykLoading = Y.t().mul(xkScore).div(xkScore.norm2squared());
	  Y.subi(xkScore.mul(ykLoading.t()));
	  break;
      }


      // Store results
      m_XScores.setColumn(k, xkScore);
      m_YScores.setColumn(k, ykScore);
      m_XWeights.setColumn(k, xkWeight);
      m_YWeights.setColumn(k, ykWeight);
      m_XLoadings.setColumn(k, xkLoading);
      m_YLoadings.setColumn(k, ykLoading);


    }

    m_X = X;
    m_XRotations = m_XWeights.mul((m_XLoadings.t().mul(m_XWeights)).inverse());
    if (Y.numColumns() > 1) {
      m_YRotations = m_YWeights.mul((m_YLoadings.t().mul(m_YWeights)).inverse());
    }
    else {
      m_YRotations = MatrixFactory.filled(1, 1, 1.0);
    }

    // Calculate regression coefficients
    Matrix yStds = MatrixFactory.fromColumn(m_StandardizeY.getStdDevs());
    m_Coef = m_XRotations.mul(m_YLoadings.t()).scaleByVector(yStds);
    return null;
  }

  /**
   * Perform the inner NIPALS loop
   *
   * @param X Predictors Matrix
   * @param Y Response Matrix
   * @return NipalsLoopResult
   */
  protected NipalsLoopResult nipalsLoop(Matrix X, Matrix Y) {
    int iterations = 0;

    Matrix yScore = Y.getColumn(0); // (y scores)
    Matrix xWeight;
    Matrix xWeightOld = MatrixFactory.zeros(X.numColumns(), 1);
    Matrix yWeight;
    Matrix xScore;

    double eps = 1e-16;

    // Repeat 1) - 3) until convergence: either change of u is lower than m_Tol or maximum
    // number of iterations has been reached (m_MaxIter)
    while (true) {
      // 1) Update X weights
      xWeight = X.t().mul(yScore).div(yScore.norm2squared());

      // Add eps if necessary to converge to a more acceptable solution
      if (xWeight.norm2squared() < eps) {
	xWeight.addi(eps);
      }

      // Normalize
      xWeight.divi(Math.sqrt(xWeight.norm2squared()) + eps);


      // 2) Calculate latent X scores
      xScore = X.mul(xWeight);

      // 3) Update Y weights
      yWeight = Y.t().mul(xScore).div(xScore.norm2squared());

      // Normalize Y weights
      if (m_NormYWeights) {
	yWeight.divi(Math.sqrt(yWeight.norm2squared()) + eps);
      }

      // 4) Calculate ykScores
      yScore = Y.mul(yWeight).div(yWeight.norm2squared() + eps);

      Matrix xWeightDiff = xWeight.sub(xWeightOld);

      if (xWeightDiff.norm2squared() < m_Tol || Y.numColumns() == 1) {
	break;
      }

      if (iterations >= m_MaxIter) {
	break;
      }

      // Update stopping conditions
      iterations++;
    }

    return new NipalsLoopResult(xWeight, yWeight, iterations);
  }

  @Override
  protected Matrix doPerformPredictions(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    Matrix yMeans = MatrixFactory.fromColumn(m_StandardizeY.getMeans());
    Matrix Y_hat = X.mul(m_Coef).addByVector(yMeans);
    return Y_hat;
  }

  @Override
  protected Matrix doTransform(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    // Apply rotations
    Matrix xScores = X.mul(m_XRotations);
    return xScores;
  }

  protected Matrix doTransformResponse(Matrix response) {
    Matrix Y = m_StandardizeY.transform(response);

    // Apply rotations
    Matrix yScores = Y.mul(m_YRotations);
    return yScores;
  }

  @Override
  public String[] getMatrixNames() {
    return new String[]{"T", "U", "P", "Q"};
  }

  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      case "T":
	return m_XScores;
      case "U":
	return m_YScores;
      case "P":
	return m_XLoadings;
      case "Q":
	return m_YLoadings;
    }
    return null;
  }

  @Override
  public boolean hasLoadings() {
    return true;
  }

  @Override
  protected void reset() {
    super.reset();
    m_XScores = null;
    m_YScores = null;
    m_XLoadings = null;
    m_YLoadings = null;
    m_XWeights = null;
    m_YWeights = null;
    m_Coef = null;
    m_X = null;
    m_NormYWeights = false;
    m_deflationMode = DeflationMode.REGRESSION;
    m_XRotations = null;
    m_YRotations = null;
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
  }

  @Override
  public Matrix getLoadings() {
    return m_XLoadings;
  }

  @Override
  public boolean canPredict() {
    return true;
  }

  public Matrix getCoef() {
    return m_Coef;
  }

  /**
   * NIPALS loop result: x and y weight matrices and number of iterations.
   */
  private class NipalsLoopResult {

    Matrix xWeights;

    Matrix yWeights;

    int iterations;

    public NipalsLoopResult(Matrix xWeights, Matrix yWeights, int iterations) {
      this.xWeights = xWeights;
      this.yWeights = yWeights;
      this.iterations = iterations;
    }
  }

  /**
   * Deflation mode Enum.
   */
  public enum DeflationMode {
    CANONICAL,
    REGRESSION
  }
}
