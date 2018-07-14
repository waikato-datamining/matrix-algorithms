package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.transformation.Standardize;

import java.util.Random;

/**
 * @author Steven Lang
 */
public class NIPALS extends AbstractMultiResponsePLS {

  private static final long serialVersionUID = -2760078672082710402L;

  public static final int SEED = 0;

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

  /** Training points */
  protected Matrix m_X;


  /** Inner NIPALS loop improvement tolerance */
  protected double m_Tol;

  /** Inner NIPALS loop maximum number of iterations */
  protected int m_MaxIter;

  /** Standardize X transformation */
  protected Standardize m_StandardizeX;

  /** Standardize Y transformation */
  protected Standardize m_StandardizeY;

  @Override
  protected void initialize() {
    super.initialize();
    setTol(1e-6);
    setMaxIter(500);
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
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

  @Override
  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
    Matrix X, Y, I, xkScore, ykScore, xkLoading, ykLoading, xkWeight, ykWeight;


    getLogger();

    // Init
    m_X = predictors;
    m_X = m_StandardizeX.transform(m_X);
    Y = response;
    Y = m_StandardizeY.transform(Y);

    // Dimensions
    int numRows = m_X.numRows();
    int numFeatures = m_X.numColumns();
    int numClasses = Y.numColumns();
    int numComponents = getNumComponents();


    m_XScores = new Matrix(numRows, numComponents); // T
    m_YScores = new Matrix(numRows, numComponents); // U

    m_XWeights = new Matrix(numFeatures, numComponents); // W
    m_YWeights = new Matrix(numClasses, numComponents); // C

    m_XLoadings = new Matrix(numFeatures, numComponents); // P
    m_YLoadings = new Matrix(numClasses, numComponents); // Q

    xkScore = new Matrix(numRows, 1);
    ykScore = new Matrix(numRows, 1);

    xkWeight = new Matrix(numFeatures, 1);
    ykWeight = new Matrix(numClasses, 1);

    xkLoading = new Matrix(numFeatures, 1);
    ykLoading = new Matrix(numClasses, 1);

    double eps = 1e-10;

    Random rng = new Random(SEED);
    for (int currentComponent = 0; currentComponent < numComponents; currentComponent++) {
      int iterations = 0;
      Matrix xkScoreOld;
      int randomClassIndex = rng.nextInt(Y.numColumns());
      ykScore = Y.getColumn(randomClassIndex); // (y scores)

      double iterationChange = m_Tol * 10;

      // Repeat 1) - 3) until convergence: either change of u is lower than m_Tol or maximum
      // number of iterations has been reached (m_MaxIter)
      while (iterationChange > m_Tol && iterations < m_MaxIter) {
	// 1) Calculate xkWeights
	xkWeight = m_X.t().mul(ykScore).div(ykScore.norm2squared());

	// Add eps if necessary to converge to a more acceptable solution
	if(xkWeight.t().mul(xkWeight).asDouble() < eps){
	  xkWeight.addi(eps);
        }

        // Normalize
        xkWeight.divi(Math.sqrt(xkWeight.norm2squared()) + eps);


	// 2) Calculate xkScores
        xkScoreOld = xkScore;
        xkScore = m_X.mul(xkWeight);

        // 3) Calculate ykWeights
        ykWeight = Y.t().mul(xkScore).div(xkScore.norm2squared());

        // 4) Caluclate ykScores
	ykScore = Y.mul(ykWeight).div(ykWeight.norm2squared() + eps);

	// Update stopping conditions
	iterations++;
	iterationChange = xkScore.sub(xkScoreOld).norm2();
      }


      xkLoading = m_X.t().mul(xkScore).div(xkScore.norm2squared());
      ykLoading = Y.t().mul(xkScore).div(xkScore.norm2squared());


      // Store results
      m_XScores.setColumn(currentComponent, xkScore);
      m_YScores.setColumn(currentComponent, ykScore);
      m_XWeights.setColumn(currentComponent, xkWeight);
      m_YWeights.setColumn(currentComponent, ykWeight);
      m_XLoadings.setColumn(currentComponent, xkLoading);
      m_YLoadings.setColumn(currentComponent, ykLoading);


      // Deflate X and Y
      m_X.subi(xkScore.mul(xkLoading.t()));
      Y.subi(xkScore.mul(ykLoading.t()));
    }

    m_XRotations = m_XWeights.mul((m_XLoadings.t().mul(m_XWeights)).inverse());

    return null;
  }


  @Override
  protected Matrix doPerformPredictions(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    // Y = X W(P'W)^-1Q' + Err = XB + Err
    //            # => B = W*Q' (p x q)

    Matrix yStds = Matrix.fromColumn(m_StandardizeY.getStdDevs());

    Matrix coef = m_XRotations.mul(m_YLoadings.t()).scaleByVector(yStds);
    Matrix Y_hat = X.mul(coef);
    return Y_hat;
  }

  @Override
  protected Matrix doTransform(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    return X.mul(m_XRotations);
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
    m_X = null;
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
  }

  @Override
  public Matrix getLoadings() {
    return m_XScores;
  }

  @Override
  public boolean canPredict() {
    return true;
  }


}
