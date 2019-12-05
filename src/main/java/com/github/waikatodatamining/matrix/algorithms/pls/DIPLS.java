package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.algorithms.Center;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.matrix.MatrixFactory;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain Invariant Partial Least Squares.
 *
 * <a href="https://pubs.acs.org/doi/10.1021/acs.analchem.8b00498">Domain-Invariant Partial-Least-Squares Regression</a>
 *
 * Parameters:
 * - lambda: Influence of domain variance differences into PLS weighting
 *
 * @author Steven Lang
 */
public class DIPLS
  extends AbstractSingleResponsePLS {

  private static final long serialVersionUID = 2782575841430129392L;

  /** Model adaption strategy */
  protected ModelAdaptionStrategy m_modelAdaptionStrategy = ModelAdaptionStrategy.UNSUPERVISED;

  /** Number of source train samples */
  protected int m_ns;

  /** Number of target train samples */
  protected int m_nt;

  /** Lambda parameter */
  protected double m_lambda = 1.0;

  /** Response mean */
  protected double m_b0;

  /** Loadings */
  protected Matrix m_T;

  /** Source domain loadings */
  protected Matrix m_Ts;

  /** Target domain loadings */
  protected Matrix m_Tt;

  /** Scores */
  protected Matrix m_P;

  /** Source domain scores */
  protected Matrix m_Ps;

  /** Target domain scores */
  protected Matrix m_Pt;

  /** Weights */
  protected Matrix m_Wdi;

  /** Regression coefficients */
  protected Matrix m_bdi;

  /** X center */
  protected Center m_Xcenter = new Center();

  /** Source domain X center */
  protected Center m_Xscenter = new Center();

  /** Target domain X center */
  protected Center m_Xtcenter = new Center();

  @Override
  protected void doReset() {
    super.doReset();
    m_T = null;
    m_Ts = null;
    m_Tt = null;
    m_P = null;
    m_Ps = null;
    m_Pt = null;
    m_Wdi = null;
    m_bdi = null;
    m_b0 = Double.NaN;
  }

  /**
   * Get the lambda parameter
   *
   * @return Lambda
   */
  public double getLambda() {
    return m_lambda;
  }

  /**
   * Set the lambda parameter. Must be != 0.
   *
   * @param lambda Lambda
   */
  public void setLambda(double lambda) {
    if (Math.abs(lambda) < 1e-8) {
      getLogger().warning("Lambda must be != 0 but was " + lambda + ".");
    }
    else {
      m_lambda = lambda;
      reset();
    }
  }

  @Override
  public String[] getMatrixNames() {
    return new String[]{
      "T", "Ts", "Tt",
      "Wdi",
      "P", "Ps", "Pt",
      "bdi"
    };
  }

  @Override
  public Matrix getMatrix(String name) {
    Map<String, Matrix> map = new HashMap<>();
    map.put("T", m_T);
    map.put("Ts", m_Ts);
    map.put("Tt", m_Tt);
    map.put("P", m_P);
    map.put("Ps", m_Ps);
    map.put("Pt", m_Pt);
    map.put("Wdi", m_Wdi);
    map.put("bdi", m_bdi);

    if (map.containsKey(name)) {
      return map.get(name);
    }

    return null;
  }

  @Override
  public boolean hasLoadings() {
    return true;
  }

  @Override
  public Matrix getLoadings() {
    return m_T;
  }

  @Override
  public boolean canPredict() {
    return true;
  }

  @Override
  protected Matrix doPLSTransform(Matrix predictors) {
    return m_Xcenter.transform(predictors).mul(m_Wdi);
  }

  @Override
  protected void doPLSConfigure(Matrix predictors, Matrix response) {
    Matrix X = null;
    Matrix Xs = null;
    Matrix Xt = null;
    Matrix y = null;
    Matrix wdi, t, ts, tt, p, ps, pt, ca;
    Matrix c = null;
    int numFeatures = predictors.numColumns();
    Matrix I = MatrixFactory.eye(numFeatures);

    // Check if correct initialization method was called
    if (m_ns == 0 || m_nt == 0)
      throw new MatrixAlgorithmsException(
            "DIPLS must be initialized with one of the three following methods:\n" +
                  " - configureSupervised\n" +
                  " - configureSemiSupervised\n" +
                  " - initializeUnsupervisedSupervised\n");

    // Check if sufficient source and target samples exist
    if (m_ns == 1 || m_nt == 1) {
      throw new MatrixAlgorithmsException("Number of source and target samples has to be > 1");
    }

    // Initialize Xs, Xt, X, y
    switch (m_modelAdaptionStrategy) {
      case UNSUPERVISED:
	Xs = predictors.getRows(0, m_ns);
	Xt = predictors.getRows(m_ns, predictors.numRows());

	X = Xs.copy();
	y = response;
	break;
      case SUPERVISED:
	Xs = predictors.getRows(0, m_ns);
	Xt = predictors.getRows(m_ns, predictors.numRows());

	// X = [Xs, Xt]
	X = predictors;
	y = response;
	break;
      case SEMISUPERVISED:
	Xs = predictors.getRows(0, m_ns);
	Xt = predictors.getRows(m_ns, predictors.numRows());

	// X = [Xs, Xt] but without Xt_unlabeled
	X = predictors.getRows(0, m_ns + m_ns);
	y = response;
	break;
    }

    // Center X, Xs, Xt
    X = m_Xcenter.configureAndTransform(X);
    Xs = m_Xscenter.configureAndTransform(Xs);
    Xt = m_Xtcenter.configureAndTransform(Xt);

    // Center y
    m_b0 = y.mean(-1).asDouble();
    y = y.sub(m_b0);

    // Start loop over number of components
    for (int a = 0; a < getNumComponents(); a++) {

      // Calculate domain invariant weights
      double yNorm2Squared = y.norm2squared();
      Matrix wdiLHS = y.t().mul(X).div(yNorm2Squared);
      Matrix XstXs = Xs.t().mul(Xs).mul(1.0 / (m_ns - 1.0));
      Matrix XttXt = Xt.t().mul(Xt).mul(1.0 / (m_nt - 1.0));
      Matrix XsDiffXt = XstXs.sub(XttXt);
      Matrix wdiRHS = I.add(XsDiffXt.mul(m_lambda / (2 * yNorm2Squared))).inverse();
      wdi = wdiLHS.mul(wdiRHS).t();
      wdi = wdi.normalized();

      // Calculate loadings
      t = X.mul(wdi);
      ts = Xs.mul(wdi);
      tt = Xt.mul(wdi);

      // Calculate scores
      p = (t.t().mul(t)).inverse().mul(t.t()).mul(X);
      ps = (ts.t().mul(ts)).inverse().mul(ts.t()).mul(Xs);
      pt = (tt.t().mul(tt)).inverse().mul(tt.t()).mul(Xt);
      ca = (t.t().mul(t)).inverse().mul(y.t()).mul(t);

      // Deflate X, Xs, Xt, y
      X = X.sub(t.mul(p));
      Xs = Xs.sub(ts.mul(ps));
      Xt = Xt.sub(tt.mul(pt));
      y = y.sub(t.mul(ca));

      // Collect
      c = concat(c, ca);

      m_T = concat(m_T, t);
      m_Ts = concat(m_Ts, ts);
      m_Tt = concat(m_Tt, tt);

      m_P = concat(m_P, p.t());
      m_Ps = concat(m_Ps, ps.t());
      m_Pt = concat(m_Pt, pt.t());

      m_Wdi = concat(m_Wdi, wdi);
    }

    // Calculate regression coefficients
    m_bdi = m_Wdi.mul((m_P.t().mul(m_Wdi)).inverse()).mul(c.t());
  }

  /**
   * Concat A along columns with a. If A is null, return a.
   *
   * @param A Base matrix
   * @param a Column vector
   * @return Concatenation of A and a
   */
  private Matrix concat(Matrix A, Matrix a) {
    if (A == null) {
      return a;
    }
    else {
      return A.concatAlongColumns(a);
    }
  }

  @Override
  protected Matrix doPLSPredict(Matrix predictors) {

    Matrix recentered = null;

    // Recenter
    // TODO: m_Xscenter is not used here (Possible bug?)
    switch (m_modelAdaptionStrategy) {
      case UNSUPERVISED:
        recentered = m_Xtcenter.transform(predictors);
        break;
      case SUPERVISED:
        recentered = m_Xcenter.transform(predictors);
        break;
      case SEMISUPERVISED:
        recentered = m_Xcenter.transform(predictors);
        break;
    }

    // Predict
    Matrix regression = recentered.mul(m_bdi);

    // Add response means
    return regression.add(m_b0);
  }

  /**
   * Unsupervised initialization.
   *
   * @param predictorsSourceDomain Predictors from source domain
   * @param predictorsTargetDomain Predictors from target domain
   * @param responseSourceDomain   Response from source domain
   */
  public void configureUnsupervised(Matrix predictorsSourceDomain,
                                    Matrix predictorsTargetDomain,
                                    Matrix responseSourceDomain) {
    m_ns = predictorsSourceDomain.numRows();
    m_nt = predictorsTargetDomain.numRows();
    m_modelAdaptionStrategy = ModelAdaptionStrategy.UNSUPERVISED;
    Matrix X = predictorsSourceDomain.concatAlongRows(predictorsTargetDomain);
    Matrix y = responseSourceDomain;

    configure(X, y);
  }

  /**
   * Supervised initialization.
   *
   * @param predictorsSourceDomain Predictors from source domain
   * @param predictorsTargetDomain Predictors from target domain
   * @param responseSourceDomain   Response from source domain
   * @param responseTargetDomain   Response from target domain
   */
  public void configureSupervised(Matrix predictorsSourceDomain,
                                  Matrix predictorsTargetDomain,
                                  Matrix responseSourceDomain,
                                  Matrix responseTargetDomain) {
    m_ns = predictorsSourceDomain.numRows();
    m_nt = predictorsTargetDomain.numRows();
    m_modelAdaptionStrategy = ModelAdaptionStrategy.SUPERVISED;
    Matrix X = predictorsSourceDomain.concatAlongRows(predictorsTargetDomain);
    Matrix y = responseSourceDomain.concatAlongRows(responseTargetDomain);

    configure(X, y);
  }

  /**
   * Semisupervised initialization.
   *
   * @param predictorsSourceDomain          Predictors from source domain
   * @param predictorsTargetDomain          Predictors from target domain
   * @param predictorsTargetDomainUnlabeled Predictors from target domain
   *                                        without labels
   * @param responseSourceDomain            Response from source domain
   * @param responseTargetDomain            Response from target domain
   */
  public void configureSemiSupervised(Matrix predictorsSourceDomain,
                                      Matrix predictorsTargetDomain,
                                      Matrix predictorsTargetDomainUnlabeled,
                                      Matrix responseSourceDomain,
                                      Matrix responseTargetDomain) {
    m_ns = predictorsSourceDomain.numRows();
    m_nt = predictorsTargetDomain.numRows();
    m_modelAdaptionStrategy = ModelAdaptionStrategy.SEMISUPERVISED;
    Matrix X = predictorsSourceDomain
      .concatAlongRows(predictorsTargetDomain)
      .concatAlongRows(predictorsTargetDomainUnlabeled);
    Matrix y = responseSourceDomain.concatAlongRows(responseTargetDomain);

    configure(X, y);
  }

  /**
   * Model Adaption Strategy. Indicates, how to initialize the algorithm.
   */
  protected enum ModelAdaptionStrategy {
    UNSUPERVISED,
    SUPERVISED,
    SEMISUPERVISED
  }
}
