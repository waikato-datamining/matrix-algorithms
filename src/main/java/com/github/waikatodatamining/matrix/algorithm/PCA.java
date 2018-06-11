/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PCA.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import Jama.EigenvalueDecomposition;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.Utils;
import com.github.waikatodatamining.matrix.transformation.AbstractTransformation;
import com.github.waikatodatamining.matrix.transformation.Center;
import com.github.waikatodatamining.matrix.transformation.Standardize;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs principal components analysis and allows access to loadings and scores.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PCA
  extends AbstractAlgorithm {

  private static final long serialVersionUID = 7150143741822676345L;

  /** the variance to cover. */
  protected double m_Variance;

  /** the maximum number of attributes. */
  protected int m_MaxColumns;

  /** whether to center (rather than standardize) the data and compute PCA from
   * covariance (rather than correlation) matrix. */
  protected boolean m_Center;

  /** the loadings. */
  protected Matrix m_Loadings;

  /** the scores. */
  protected Matrix m_Scores;

  /** Correlation matrix for the original data. */
  protected double[][] m_Correlation;

  /** Will hold the unordered linear transformations of the (normalized) original data. */
  protected double[][] m_Eigenvectors;

  /** Eigenvalues for the corresponding eigenvectors. */
  protected double[] m_Eigenvalues;

  /** Sorted eigenvalues. */
  protected int[] m_SortedEigens;

  /** sum of the eigenvalues. */
  protected double m_SumOfEigenValues;

  /** Number of columns. */
  protected int m_NumCols;

  /** Number of rows. */
  protected int m_NumRows;

  /** the columns to delete. */
  protected TIntList m_KeepCols;

  /** the matrix used for training. */
  protected Matrix m_Train;

  /** the filter for transforming the data. */
  protected AbstractTransformation m_Transformation;

  /**
   * Initializes the algorithm.
   */
  @Override
  protected void initialize() {
    super.initialize();
    setVariance(0.95);
    setMaxColumns(-1);
    setCenter(false);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Loadings         = null;
    m_Scores           = null;
    m_Correlation      = null;
    m_Eigenvectors     = null;
    m_Eigenvalues      = null;
    m_SortedEigens     = null;
    m_SumOfEigenValues = 0.0;
    m_Transformation = null;
  }

  /**
   * Sets the variance.
   *
   * @param value	the variance
   */
  public void setVariance(double value) {
    if ((value > 0.0) && (value < 1.0)) {
      m_Variance = value;
      reset();
    }
  }

  /**
   * Returns the variance.
   *
   * @return		the variance
   */
  public double getVariance() {
    return m_Variance;
  }

  /**
   * Sets the maximum attributes.
   *
   * @param value	the maximum
   */
  public void setMaxColumns(int value) {
    if ((value == -1) || (value > 0)) {
      m_MaxColumns = value;
      reset();
    }
  }

  /**
   * Returns the maximum attributes.
   *
   * @return		the maximum
   */
  public int getMaxColumns() {
    return m_MaxColumns;
  }

  /**
   * Set whether to center (rather than standardize) the data. If set to true
   * then PCA is computed from the covariance rather than correlation matrix.
   *
   * @param center true if the data is to be centered rather than standardized
   */
  public void setCenter(boolean center) {
    m_Center = center;
    reset();
  }

  /**
   * Get whether to center (rather than standardize) the data. If true then PCA
   * is computed from the covariance rather than correlation matrix.
   *
   * @return true if the data is to be centered rather than standardized.
   */
  public boolean getCenter() {
    return m_Center;
  }

  /**
   * Returns the loadings.
   *
   * @return		the loadings, null if not available
   */
  public Matrix getLoadings() {
    return m_Loadings;
  }

  /**
   * Returns the scores.
   *
   * @return		the scores, null if not available
   */
  public Matrix getScores() {
    return m_Scores;
  }

  /**
   * Fills the covariance matrix.
   */
  protected void fillCorrelation() {
    m_Correlation = new double[m_NumCols][m_NumCols];

    for (int i = 0; i < m_NumCols; i++) {
      for (int j = i; j < m_NumCols; j++) {
        double cov = 0;
        for (int n = 0; n < m_Train.numRows(); n++)
          cov += m_Train.get(n, i) * m_Train.get(n, j);

        cov /= m_Train.numRows() - 1;
        m_Correlation[i][j] = cov;
        m_Correlation[j][i] = cov;
      }
    }
  }

  /**
   * Removes the columns according to {@link #m_KeepCols}.
   *
   * @param data	the data to trim
   * @return		the trimmed data
   */
  protected Matrix removeColumns(Matrix data) {
    TIntList 	rows;
    int		j;

    if (m_KeepCols.size() != data.numColumns()) {
      rows = new TIntArrayList();
      for (j = 0; j < data.numRows(); j++)
	rows.add(j);
      data = data.getSubMatrix(rows.toArray(), m_KeepCols.toArray());
    }

    return data;
  }

  /**
   * Initializes the filter with the given input data.
   *
   * @param instances the data to process
   * @throws Exception in case the processing goes wrong
   */
  protected void configure(Matrix instances) throws Exception {
    int 			i;
    int 			j;
    double[][] 			v;
    Matrix 			corr;
    EigenvalueDecomposition 	eig;
    Matrix 			V;
    TDoubleSet 			distinct;

    m_Train = instances.copy();

    // delete any attributes with only one distinct value or are all missing
    m_KeepCols = new TIntArrayList();
    for (j = 0; j < m_Train.numColumns(); j++) {
      distinct = new TDoubleHashSet();
      for (i = 0; i < m_Train.numRows(); i++) {
	distinct.add(m_Train.get(i, j));
	if (distinct.size() > 1)
	  break;
      }
      if (distinct.size() > 1)
	m_KeepCols.add(j);
    }
    m_Train = removeColumns(m_Train);

    // transform data
    if (m_Center)
      m_Transformation = new Center();
    else
      m_Transformation = new Standardize();
    m_Train = m_Transformation.transform(m_Train);

    m_NumRows = m_Train.numRows();
    m_NumCols = m_Train.numColumns();

    fillCorrelation();

    // get eigen vectors/values
    corr = new Matrix(m_Correlation);
    V    = corr.getEigenvalues();
    v    = new double[m_NumCols][m_NumCols];
    for (i = 0; i < v.length; i++) {
      for (j = 0; j < v[0].length; j++)
	v[i][j] = V.get(i, j);
    }
    m_Eigenvectors = v.clone();
    m_Eigenvalues = corr.getEigenvalues().toRawCopy1D();

    // any eigenvalues less than 0 are not worth anything --- change to 0
    for (i = 0; i < m_Eigenvalues.length; i++) {
      if (m_Eigenvalues[i] < 0)
	m_Eigenvalues[i] = 0.0;
    }
    m_SortedEigens = Utils.sort(m_Eigenvalues);
    m_SumOfEigenValues = Utils.sum(m_Eigenvalues);

    m_Train = null;
  }

  /**
   * Transform a matrix.
   *
   * @param data	the original data to transform
   * @return 		the transformed data
   * @throws Exception 	if data can't be transformed
   */
  protected Matrix doTransform(Matrix data) throws Exception {
    Matrix 	result;
    double[] 	newVals;
    double[][]	values;
    double 	cumulative;
    int 	i;
    int 	j;
    int 	n;
    double 	val;
    int 	numColsLowerBound;
    int 	numCols;
    int 	numColsAct;
    int		cols;

    numCols = (m_MaxColumns > 0) ? m_MaxColumns : m_NumCols;
    if (m_MaxColumns > 0)
      numColsLowerBound = m_NumCols - m_MaxColumns;
    else
      numColsLowerBound = 0;
    if (numColsLowerBound < 0)
      numColsLowerBound = 0;

    data       = removeColumns(data);
    data       = m_Transformation.transform(data);
    values     = new double[data.numRows()][];
    numColsAct = 0;
    for (n = 0; n < data.numRows(); n++) {
      newVals = new double[numCols];

      cumulative = 0;
      cols       = 0;
      for (i = m_NumCols - 1; i >= numColsLowerBound; i--) {
        cols++;
	val = 0.0;
	for (j = 0; j < m_NumCols; j++)
	  val += m_Eigenvectors[j][m_SortedEigens[i]] * data.get(n, j);

	newVals[m_NumCols - i - 1] = val;
	cumulative += m_Eigenvalues[m_SortedEigens[i]];
	if ((cumulative / m_SumOfEigenValues) >= m_Variance)
	  break;
      }
      numColsAct = Math.max(numColsAct, cols);
      values[n] = newVals;
    }

    if (getDebug())
      getLogger().info("numColsAct: " + numColsAct);

    // generate matrix based on actual number of retained columns
    result = new Matrix(data.numRows(), numColsAct);
    for (n = 0; n < values.length; n++) {
      for (i = 0; i < values[n].length && i < numColsAct; i++)
        result.set(n, i, values[n][i]);
    }

    return result;
  }

  /**
   * Get the components from the principal components model
   *
   * @return		2D array containing the coefficients
   */
  protected List<List<Double>> getCoefficients() {
    List<List<Double>> 	result;
    List<Double> 	onePC;
    double 		cumulative;
    int 		i;
    int 		j;
    double 		coeffValue;
    int 		numColsLowerBound;

    if (m_Eigenvalues == null)
      return null;

    if (m_MaxColumns > 0)
      numColsLowerBound = m_NumCols - m_MaxColumns;
    else
      numColsLowerBound = 0;

    if (numColsLowerBound < 0)
      numColsLowerBound = 0;

    //all the coefficients for a single principal component
    result     = new ArrayList<>();
    cumulative = 0.0;
    //loop through each principle component
    for (i = m_NumCols - 1; i >= numColsLowerBound; i--) {
      onePC = new ArrayList<>();

      for (j = 0; j < m_NumCols; j++) {
	coeffValue = m_Eigenvectors[j][m_SortedEigens[i]];
	onePC.add(coeffValue);
      }

      result.add(onePC);
      cumulative += m_Eigenvalues[m_SortedEigens[i]];

      if ((cumulative / m_SumOfEigenValues) >= m_Variance)
	break;
    }

    return result;
  }

  /**
   * Create a matrix to output from the coefficients 2D array
   *
   * @return		matrix containing the components
   * @see		#getCoefficients()
   */
  protected Matrix extractLoadings() {
    Matrix 		result;
    int			i;
    int			n;
    List<List<Double>> 	coeff;

    coeff  = getCoefficients();
    result = new Matrix(m_NumCols, coeff.size() + 1);

    // add the index column
    for (n = 0; n < m_NumCols; n++)
      result.set(n, result.numColumns() - 1, n+1);

    //each arraylist is a single column
    for (i = 0; i< coeff.size() ; i++) {
      for (n = 0; n < m_NumCols; n++) {
	// column was kept earlier
	double value = 0.0;
	if (m_KeepCols.contains(n)) {
	  int index = m_KeepCols.indexOf(n);
	  if (index < coeff.get(i).size())
	    value = coeff.get(i).get(index);
	}
	result.set(n, i, value);
      }
    }

    return result;
  }

  /**
   * Transforms the data.
   *
   * @param data	the data to transform
   * @return		the transformed data
   * @throws Exception	if analysis fails
   */
  public Matrix transform(Matrix data) throws Exception {
    Matrix	result;
    
    reset();
    configure(data);
    result     = doTransform(data);
    m_Scores   = result;
    m_Loadings = extractLoadings();

    return result;
  }

  /**
   * For outputting some information about the algorithm.
   *
   * @return		the information
   */
  public String toString() {
    StringBuilder	result;

    result = new StringBuilder();
    result.append(getClass().getName()).append("\n");
    result.append(getClass().getName().replaceAll(".", "=")).append("\n\n");
    result.append("Debug      : " + getDebug()).append("\n");
    result.append("Variance   : " + getVariance()).append("\n");
    result.append("Max columns: " + getMaxColumns()).append("\n");
    result.append("Center     : " + getCenter()).append("\n");

    return result.toString();
  }
}
