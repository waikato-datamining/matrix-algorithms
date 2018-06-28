package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import com.github.waikatodatamining.matrix.transformation.Center;
import com.github.waikatodatamining.matrix.transformation.Standardize;
import com.github.waikatodatamining.matrix.transformation.kernel.AbstractKernel;
import com.github.waikatodatamining.matrix.transformation.kernel.RBFKernel;

/**
 *
 *
 * @author Steven Lang
 */
public class NIPALS extends AbstractMultiResponsePLS {

  private static final long serialVersionUID = -2760078672082710402L;

  public static final int SEED = 0;

  /** Scores on X */
  protected Matrix m_T;

  /** Scores on Y */
  protected Matrix m_U;

  /** Loadings on X */
  protected Matrix m_P;

  /** Loadings on Y */
  protected Matrix m_Q;

  /** Partial regression matrix */
  protected Matrix m_B;

  /** Training points */
  protected Matrix m_X;

  /** Kernel for feature transformation */
  protected AbstractKernel m_Kernel;


  /** Inner NIPALS loop improvement tolerance */
  protected double m_Tol;

  /** Inner NIPALS loop maximum number of iterations */
  protected int m_MaxIter;

  /** Standardize X transformation */
  protected Standardize m_StandardizeX;

  /** Center Y transformation */
  protected Center m_CenterY;

  @Override
  protected void initialize() {
    super.initialize();
    setKernel(new RBFKernel());
    setTol(1e-6);
    setMaxIter(500);
    m_StandardizeX = new Standardize();
    m_CenterY = new Center();
  }

  public AbstractKernel getKernel() {
    return m_Kernel;
  }

  public void setKernel(AbstractKernel kernel) {
    this.m_Kernel = kernel;
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
    Matrix X, Y, I, t, u, q, w, p;

    getLogger();
    // Init
    int numComponents = getNumComponents();
    m_X = predictors;
    m_X = m_StandardizeX.transform(m_X);
    Y = response;
    Y = m_CenterY.transform(Y);

    int numRows = m_X.numRows();
    int numClasses = Y.numColumns();

    q = new Matrix(numClasses, 1);
    p = new Matrix(numRows, 1);
    t = new Matrix(numRows, 1);
    w = new Matrix(numRows, 1);
    I = Matrix.identity(numRows, numRows);

    m_T = new Matrix(numRows, numComponents);
    m_U = new Matrix(numRows, numComponents);
    m_P = new Matrix(numRows, numComponents);
    m_Q = new Matrix(numClasses, numComponents);


    for (int currentComponent = 0; currentComponent < numComponents; currentComponent++) {
      int iterations = 0;
      Matrix uOld;
      u = MatrixHelper.randn(numRows, 1, SEED + currentComponent);
      double iterationChange = m_Tol * 10;

      // Repeat 1) - 3) until convergence: either change of u is lower than m_Tol or maximum
      // number of iterations has been reached (m_MaxIter)
      while (iterationChange > m_Tol && iterations < m_MaxIter) {
        // 1) Calculate p
        p = m_X.transpose().mul(u).normalized();

	// 2) Calculate t
	t = m_X.mul(p);

	// 3) Calculate q
	q = Y.transpose().mul(t).normalized();

	// 4) Calculate u
	uOld = u;
	u = Y.mul(q);

	// Update stopping conditions
	iterations++;
	iterationChange = u.sub(uOld).norm2();
      }

      // Deflate
      m_X.subi(t.mul(p.transpose()));
      Y.subi(u.mul(q.transpose()));

      // Store u,t,c,p
      m_T.setColumn(currentComponent, t);
      m_U.setColumn(currentComponent, u);
      m_Q.setColumn(currentComponent, q);
      m_P.setColumn(currentComponent, p);
    }

    // Regress U = TB
    m_B = m_T.transpose().mul(m_T).inverse().mul(m_T.transpose()).mul(m_U);
    return null;
  }


  @Override
  protected Matrix doPerformPredictions(Matrix predictors) {
    Matrix K_t = doTransform(predictors);
    Matrix Y_hat = K_t.mul(m_B);
    Y_hat = m_CenterY.inverseTransform(Y_hat);
    return Y_hat;
  }

  @Override
  protected Matrix doTransform(Matrix predictors) {
    Matrix stand = m_StandardizeX.transform(predictors);

    return predictors.mul(m_T.transpose());
  }

  @Override
  public String[] getMatrixNames() {
    return new String[]{"K", "T", "U", "P", "Q"};
  }

  @Override
  public Matrix getMatrix(String name) {
    switch (name) {
      case "T":
	return m_T;
      case "U":
	return m_U;
      case "P":
	return m_P;
      case "Q":
	return m_Q;
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
    m_T = null;
    m_U = null;
    m_P = null;
    m_Q = null;
    m_B = null;
    m_X = null;
    m_StandardizeX = new Standardize();
    m_CenterY = new Center();
  }

  @Override
  public Matrix getLoadings() {
    return m_T;
  }

  @Override
  public boolean canPredict() {
    return true;
  }


}
