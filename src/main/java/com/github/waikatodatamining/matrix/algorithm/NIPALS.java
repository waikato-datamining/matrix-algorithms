///*
// *   This program is free software: you can redistribute it and/or modify
// *   it under the terms of the GNU General Public License as published by
// *   the Free Software Foundation, either version 3 of the License, or
// *   (at your option) any later version.
// *
// *   This program is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *   GNU General Public License for more details.
// *
// *   You should have received a copy of the GNU General Public License
// *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
///*
// * SIMPLS.java
// * Copyright (C) 2018 University of Waikato, Hamilton, NZ
// */
//
//package com.github.waikatodatamining.matrix.algorithm;
//
//import com.github.waikatodatamining.matrix.core.Matrix;
//import com.github.waikatodatamining.matrix.core.MatrixHelper;
//import com.github.waikatodatamining.matrix.core.Utils;
//
///**
// * NIPALS algorithm.
// * <br>
// * See here:
// * <a href="http://www.statsoft.com/Textbook/Partial-Least-Squares#SIMPLS">SIMPLS (StatSoft)</a>
// *
// * @author Steven Lang
// */
//public class NIPALS
//  extends AbstractMultiResponsePLS {
//
//  private static final long serialVersionUID = 4899661745515419256L;
//
//  /**
//   * Initializes the members.
//   */
//  @Override
//  protected void initialize() {
//    super.initialize();
//    setNumCoefficients(0);
//  }
//
//  /**
//   * Resets the member variables.
//   */
//  @Override
//  protected void reset() {
//    super.reset();
//  }
//
//  @Override
//  protected int getMinColumnsResponse() {
//    return 1;
//  }
//
//  @Override
//  protected int getMaxColumnsResponse() {
//    return -1;
//  }
//
//  /**
//   * Sets the number of coefficients of W matrix to keep (rest gets zeroed).
//   *
//   * @param value 	the number of coefficients, 0 to keep all
//   */
//  public void setNumCoefficients(int value) {
//    m_NumCoefficients = value;
//    reset();
//  }
//
//  /**
//   * returns the number of coefficients of W matrix to keep (rest gets zeroed).
//   *
//   * @return 		the maximum number of attributes, 0 to keep all
//   */
//  public int getNumCoefficients() {
//    return m_NumCoefficients;
//  }
//
//  /**
//   * Returns the all the available matrices.
//   *
//   * @return		the names of the matrices
//   */
//  @Override
//  public String[] getMatrixNames() {
//    return new String[]{
//    };
//  }
//
//  /**
//   * Returns the matrix with the specified name.
//   *
//   * @param name	the name of the matrix
//   * @return		the matrix, null if not available
//   */
//  @Override
//  public Matrix getSubMatrix(String name) {
//    switch (name) {
//      default:
//	return null;
//    }
//  }
//
//  /**
//   * Whether the algorithm supports return of loadings.
//   *
//   * @return		true if supported
//   * @see		#getLoadings()
//   */
//  public boolean hasLoadings() {
//    return true;
//  }
//
//  /**
//   * Returns the loadings, if available.
//   *
//   * @return		the loadings, null if not available
//   */
//  public Matrix getLoadings() {
//    return getSubMatrix("W");
//  }
//
//
//  /**
//   * Initializes using the provided data.
//   *
//   * @param predictors the input data
//   * @param response   the dependent variable(s)
//   * @throws Exception if analysis fails
//   * @return null if successful, otherwise error message
//   */
//  protected String doPerformInitialization(Matrix predictors, Matrix response) throws Exception {
//
//  }
//
//  /**
//   * Transforms the data.
//   *
//   * @param predictors the input data
//   * @throws Exception if analysis fails
//   * @return the transformed data
//   */
//  @Override
//  protected Matrix doTransform(Matrix predictors) throws Exception {
//  }
//
//  /**
//   * Returns whether the algorithm can make predictions.
//   *
//   * @return		true if can make predictions
//   */
//  public boolean canPredict() {
//    return true;
//  }
//
//  /**
//   * Performs predictions on the data.
//   *
//   * @param predictors the input data
//   * @throws Exception if analysis fails
//   * @return the predictions
//   */
//  @Override
//  protected Matrix doPerformPredictions(Matrix predictors) throws Exception {
//  }
//}