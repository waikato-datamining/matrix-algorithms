package com.github.waikatodatamining.matrix.algorithm.ica;

import com.github.waikatodatamining.matrix.algorithm.AbstractAlgorithm;
import com.github.waikatodatamining.matrix.algorithm.api.Filter;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.LogCosH;
import com.github.waikatodatamining.matrix.algorithm.ica.approxfun.NegEntropyApproximationFunction;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.Tuple;
import com.github.waikatodatamining.matrix.core.exceptions.MatrixAlgorithmsException;
import com.github.waikatodatamining.matrix.transformation.Center;


import static com.github.waikatodatamining.matrix.core.MatrixFactory.*;

/**
 * Fast Independent Component Analysis.
 * <p>
 * Implementation oriented at <a href="https://github.com/scikit-learn/scikit-learn/blob/f0ab589f/sklearn/decomposition/fastica_.py#L381">Scikit-Learn</a>" which is based on
 * A. Hyvarinen and E. Oja, Independent Component Analysis:
 * Algorithms and Applications, Neural Networks, 13(4-5), 2000,
 * pp. 411-430
 * <p>
 * Parameters:
 * - numComponents: Number of output signals
 * - whiten: Flag for whitening the input signal
 * - fun: functional form of G used to approximate the NegEntropy
 * - maxIter: Maximum number of iterations
 * - tol: Tolerance on iteration updates
 *
 * @author Steven Lang
 */
public class FastICA extends AbstractAlgorithm implements Filter {

  private static final long serialVersionUID = 3152829426276253757L;

  /** Number of output signals */
  protected int m_numComponents;

  /** Flag for whitening the input signal */
  protected boolean m_whiten;

  /** Functional form of G used to approximate the NegEntropy */
  protected NegEntropyApproximationFunction m_fun;

  /** Maximum number of iterations */
  protected int m_maxIter;

  /** Tolerance on iteration updates */
  protected double m_tol;

  /** Components */
  protected Matrix m_Components;

  /** Sources */
  protected Matrix m_Sources;

  /** ICA algorithm type */
  protected Algorithm m_algorithm;

  /** X center */
  protected Center m_center;

  /** Whitening matrix */
  protected Matrix m_Whitening;

  /** Mixing matrix */
  protected Matrix m_Mixing;

  /**
   * Get the number of components.
   *
   * @return Number of components
   */
  public int getNumComponents() {
    return m_numComponents;
  }

  /**
   * Set number of components.
   *
   * @param numComponents Number of components
   */
  public void setNumComponents(int numComponents) {
    if (numComponents < 1) {
      m_Logger.warning("Number of componetns must be > 0 but was " +
	numComponents + ". Falling back to " + m_numComponents + ".");
    }
    m_numComponents = numComponents;
  }

  /**
   * Get whether whiten flag is set.
   *
   * @return True if whiten is set
   */
  public boolean isWhiten() {
    return m_whiten;
  }

  /**
   * Set whether whiten flag should be set.
   *
   * @param whiten Whiten flag
   */
  public void setWhiten(boolean whiten) {
    m_whiten = whiten;
    reset();
  }

  /**
   * Get the negative entropy approximation function.
   *
   * @return Negative entropy approximation function
   */
  public NegEntropyApproximationFunction getFun() {
    return m_fun;
  }

  /**
   * Set the negative entropy approximation function.
   *
   * @param fun Negative entropy approximation function
   */
  public void setFun(NegEntropyApproximationFunction fun) {
    m_fun = fun;
  }

  /**
   * Get the maximum number of iterations.
   *
   * @return Maximum number of iterations
   */
  public int getMaxIter() {
    return m_maxIter;
  }

  /**
   * Set the maximum number of iterations.
   *
   * @param maxIter Maximum number of iterations
   */
  public void setMaxIter(int maxIter) {
    if (maxIter < 0) {
      m_Logger.warning("Maximum iterations parameter must be positive " +
	"but was " + maxIter + ".");
    }
    else {
      m_maxIter = maxIter;
      reset();
    }
  }

  /**
   * Get the iteration tolerance threshold.
   *
   * @return Iteration tolerance threshold
   */
  public double getTol() {
    return m_tol;
  }

  /**
   * Set the iteration tolerance threshold. Must be positive.
   *
   * @param tol Iteration tolerance threshold.
   */
  public void setTol(double tol) {
    if (tol < 0) {
      m_Logger.warning("Tolerance parameter must be positive but " +
	"was " + tol + ".");
    }
    else {
      m_tol = tol;
      reset();
    }
  }

  /**
   * Get the algorithm type.
   *
   * @return Algorithm type
   */
  public Algorithm getAlgorithm() {
    return m_algorithm;
  }

  /**
   * Set the algorithm type.
   *
   * @param algorithm Algorithm type
   */
  public void setAlgorithm(Algorithm algorithm) {
    m_algorithm = algorithm;
  }

  /**
   * Get components.
   *
   * @return Components
   */
  public Matrix getComponents() {
    return m_Components;
  }

  /**
   * Get sources.
   *
   * @return Decomposed sources
   */
  public Matrix getSources() {
    return m_Sources;
  }

  /**
   * Get the mixing matrix W.
   *
   * @return Mixing matrix
   */
  public Matrix getMixing() {
    return m_Mixing;
  }

  @Override
  protected void initialize() {
    super.initialize();
    m_numComponents = 5;
    m_maxIter = 500;
    m_fun = new LogCosH();
    m_tol = 1e-4;
    m_whiten = true;
    m_algorithm = Algorithm.DEFLATION;
    m_center = new Center();
  }

  @Override
  protected void reset() {
    super.reset();
    m_Components = null;
    m_Mixing = null;
    m_Whitening = null;
  }

  protected void configure(Matrix X) {
    X = X.t();

    int n = X.numRows();
    int p = X.numColumns();
    Matrix unmixing = null;
    Matrix X1;

    if (!m_whiten) {
      m_numComponents = Math.min(n, p);
      m_Logger.warning("Ignoring numComponents when $whiten=false");
    }

    if (m_numComponents > Math.min(n, p)) {
      m_Logger.warning("numComponents is too large and will be set to " +
	m_numComponents);
      m_numComponents = Math.min(n, p);
    }

    // WHiten data
    if (m_whiten) {
      X = m_center.transform(X.t()).t();
      Matrix U = X.svdU();
      Matrix d = X.getSingularValues();
      int k = Math.min(n, p); // rank k
      d = d.getRows(0, k); // Only get non zero singular values
      Matrix dInvElements = d.applyElementwise(a -> 1.0 / a);
      Matrix tmp = U.scaleByRowVector(dInvElements).transpose();
      m_Whitening = tmp.getRows(0, Math.min(tmp.numRows(), m_numComponents));

      X1 = m_Whitening.mul(X);
      X1 = X1.mul(StrictMath.sqrt(p));
    }
    else {
      X1 = X;
    }

    // Randomly initialize weights from normal dist
    Matrix Winit = randn(m_numComponents, m_numComponents, 1);

    // Use deflation algorithm
    if (Algorithm.DEFLATION.equals(m_algorithm)) {
      unmixing = deflation(X1,Winit);
    } // Use parallel algorithm
    else if (Algorithm.PARALLEL.equals(m_algorithm)) {
      unmixing = parallel(X1, Winit);
    }

    // Compute sources and components
    if (m_whiten) {
      m_Sources = unmixing.mul(m_Whitening).mul(X).t();
      m_Components = unmixing.mul(m_Whitening);
    }
    else {
      m_Sources = unmixing.mul(X).t();
      m_Components = unmixing;
    }

    m_Mixing = m_Components.inverse();
    m_Initialized = true;
  }

  /**
   * Deflationary FastICA.
   *
   * @param X Input
   * @return Weights
   */
  public Matrix deflation(Matrix X, Matrix Winit) {
    Matrix W = zeros(m_numComponents, m_numComponents);

    for (int j = 0; j < m_numComponents; j++) {
      Matrix w = Winit.getRow(j).t().copy();
      w = w.div(w.powElementwise(2).sum(-1).sqrt().asDouble());
      for (int i = 0; i < m_maxIter; i++) {
        Tuple<Matrix, Matrix> res = m_fun.apply(w.t().mul(X));

        Matrix gwtx = res.getFirst();
	Matrix g_wtx = res.getSecond();

	Matrix w1 = X.scaleByRowVector(gwtx).mean(1).sub(w.mul(g_wtx.mean()));
	w1 = decorrelate(w1, W, j);


	w1 = w1.div(w1.powElementwise(2).sum(-1).sqrt().asDouble());
	double lim = w1.mulElementwise(w).sum(-1).abs().sub(1.0).abs().asDouble();

	w = w1;
	if (lim < m_tol) {
	  break;
	}
      }

      W.setRow(j, w);
    }

    return W;
  }

  /**
   * Parallel FastICA.
   *
   * @param X Input
   * @param Winit Initial Weight matrix
   * @return Weight
   */
  public Matrix parallel(Matrix X, Matrix Winit) {
    Matrix W = symmetricDecorrelation(Winit);

    int p = X.numColumns();

    for (int i = 0; i < m_maxIter; i++) {
      Tuple<Matrix, Matrix> res = m_fun.apply(W.t().mul(X));
      Matrix gwtx = res.getFirst();
      Matrix g_wtx = res.getSecond();

      Matrix arg = gwtx.mul(X.t()).div(p).sub(W.scaleByColumnVector(g_wtx)); // scale by row?
      Matrix W1 = symmetricDecorrelation(arg);
      double lim = W1.mul(W.t()).diag().abs().sub(1.0).abs().max();
      W = W1;
      if (lim < m_tol) {
	break;
      }
    }

    return W;
  }

  /**
   * Orthonormalize w wrt to the first j columns of W.
   *
   * @param w w vector
   * @param W W matrix
   * @param j first j columns
   * @return Orthonormalized w
   */
  public Matrix decorrelate(Matrix w, Matrix W, int j) {

    if (j == 0) {
      return w;
    }
    Matrix Wp = W.getRows(0, j);

    if (j == 1){
      Matrix s = w.t().mul(Wp.t()).mul(Wp).t();
      return w.sub(s);
    }


    Matrix sub = w.mul(Wp.t()).mul(Wp);
    return w.sub(sub);
  }

  /**
   * W = (W * W.T)^(-1/2)
   *
   * @param W Weight matrix
   * @return Decorrelated weight matrix
   */
  public Matrix symmetricDecorrelation(Matrix W) {
    Matrix wwt = W.mul(W.t());
    Matrix eigvals = wwt.getEigenvaluesSortedAscending();
    Matrix eigvecs = wwt.getEigenvectorsSortedAscending();
    Matrix s = eigvals;
    Matrix u = eigvecs;

    // np.dot(np.dot(u * (1. / np.sqrt(s)), u.T), W)
    Matrix sSqrt = s.sqrt();
    Matrix sInv = sSqrt.applyElementwise(v -> 1.0 / v);
    Matrix uMuleS = u.scaleByRowVector(sInv);
    return uMuleS.mul(u.t()).mul(W);

  }

  @Override
  public String toString() {
    return "FastICA{" +
      "numComponents=" + m_numComponents +
      ", whiten=" + m_whiten +
      ", fun=" + m_fun +
      ", maxIter=" + m_maxIter +
      ", tol=" + m_tol +
      '}';
  }

  /**
   * Transform a matrix.
   *
   * @param data the original data to transform
   * @return the transformed data
   * @throws Exception if data can't be transformed
   */
  protected Matrix doTransform(Matrix data) throws Exception {
    return m_Sources;
  }

  @Override
  public Matrix transform(Matrix data) throws Exception {
    reset();
    configure(data);
    Matrix result = doTransform(data);

    return result;
  }

  public Matrix reconstruct() {
    if (isInitialized()) {
      return m_center.inverseTransform(m_Sources.mul(m_Mixing.t()).t()).t();
    }
    else {
      throw new MatrixAlgorithmsException("FastICA has not yet been " +
	"initialized!");
    }
  }

  /**
   * Algorithm type for ICA.
   */
  public enum Algorithm {
    PARALLEL,
    DEFLATION
  }
}
