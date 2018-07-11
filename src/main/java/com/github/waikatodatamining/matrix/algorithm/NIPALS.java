package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.transformation.Center;
import com.github.waikatodatamining.matrix.transformation.Standardize;

import java.util.Random;

/**
 *
 *
 * @author Steven Lang
 */
public class NIPALS extends AbstractMultiResponsePLS {

  private static final long serialVersionUID = -2760078672082710402L;

  public static final int SEED = 0;

  /** Scores on X */
  protected Matrix XScores;

  /** Scores on Y */
  protected Matrix YScores;

  /** Loadings on X */
  protected Matrix XLoadings;

  /** Loadings on Y */
  protected Matrix YLoadings;

  /** Weights on X */
  protected Matrix XWeights;

  /** Weights on Y */
  protected Matrix YWeights;

  /** Projection of X into latent space */
  protected Matrix XRotations;

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


    XScores = new Matrix(numRows, numComponents); // T
    YScores = new Matrix(numRows, numComponents); // U

    XWeights = new Matrix(numFeatures, numComponents); // W
    YWeights = new Matrix(numClasses, numComponents); // C

    XLoadings = new Matrix(numFeatures, numComponents); // P
    YLoadings = new Matrix(numClasses, numComponents); // Q

    xkScore = new Matrix(numRows, 1);
    ykScore = new Matrix(numRows, 1);

    xkWeight = new Matrix(numFeatures, 1);
    ykWeight = new Matrix(numClasses, 1);

    xkLoading = new Matrix(numFeatures, 1);
    ykLoading = new Matrix(numClasses, 1);


    Random rng = new Random(SEED);
    for (int currentComponent = 0; currentComponent < numComponents; currentComponent++) {
      int iterations = 0;
      Matrix ykScoreOld;
      int randomClassIndex = rng.nextInt(Y.numColumns());
      ykScore = Y.getColumn(randomClassIndex); // (y scores)

      double iterationChange = m_Tol * 10;

      // Repeat 1) - 3) until convergence: either change of u is lower than m_Tol or maximum
      // number of iterations has been reached (m_MaxIter)
      while (iterationChange > m_Tol && iterations < m_MaxIter) {
        // 1) Calculate w
        xkWeight = m_X.transpose().mul(ykScore).normalized();

	// 2) Calculate t
	xkScore = m_X.mul(xkWeight);

	// 3) Calculate u
        ykWeight = Y.t().mul(xkScore).normalized(); // sklearn divides by xkScore l2 norm

	ykScoreOld = ykScore;
	ykScore = Y.mul(ykWeight);

	// Update stopping conditions
	iterations++;
	iterationChange = ykScore.sub(ykScoreOld).norm2();
      }


      xkLoading = m_X.t().mul(xkScore).normalized(); // Is normalized correct here? sklearn divides by xkScore l2 norm
      ykLoading = Y.t().mul(ykScore).normalized();


      // Store results
      XScores.setColumn(currentComponent, xkScore);
      YScores.setColumn(currentComponent, ykScore);
      XWeights.setColumn(currentComponent, xkWeight);
      YWeights.setColumn(currentComponent, ykWeight);
      XLoadings.setColumn(currentComponent, xkLoading);
      YLoadings.setColumn(currentComponent, ykLoading);


      // Deflate X and Y
      m_X.subi(xkScore.mul(xkLoading.t()));
      Y.subi(ykScore.mul(ykLoading.t()));
    }

    XRotations = XWeights.mul((XLoadings.t().mul(XWeights)).inverse());

    return null;
  }


  @Override
  protected Matrix doPerformPredictions(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    // Y = X W(P'W)^-1Q' + Err = XB + Err
    //            # => B = W*Q' (p x q)

    Matrix yStds = Matrix.fromColumn(m_StandardizeY.getStdDevs());

    Matrix coef = XRotations.mul(YLoadings.t()).scaleByVector(yStds);
    Matrix Y_hat = X.mul(coef);
    return Y_hat;
  }

  @Override
  protected Matrix doTransform(Matrix predictors) {
    Matrix X = m_StandardizeX.transform(predictors);

    return X.mul(XRotations);
  }

  @Override
  public String[] getMatrixNames() {
    return new String[]{"T", "U", "P", "Q"};
  }

  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      case "T":
	return XScores;
      case "U":
	return YScores;
      case "P":
	return XLoadings;
      case "Q":
	return YLoadings;
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
    XScores = null;
    YScores = null;
    XLoadings = null;
    YLoadings = null;
    XWeights = null;
    YWeights = null;
    m_X = null;
    m_StandardizeX = new Standardize();
    m_StandardizeY = new Standardize();
  }

  @Override
  public Matrix getLoadings() {
    return XScores;
  }

  @Override
  public boolean canPredict() {
    return true;
  }


}
