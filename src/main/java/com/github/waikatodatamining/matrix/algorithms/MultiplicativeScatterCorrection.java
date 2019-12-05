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
 * MultiplicativeScatterCorrection.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.core.algorithm.MatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.matrix.MatrixHelper;
import com.github.waikatodatamining.matrix.core.algorithm.UnsupervisedMatrixAlgorithm;
import com.github.waikatodatamining.matrix.core.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs multiplicative scatter correction.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class MultiplicativeScatterCorrection
  extends UnsupervisedMatrixAlgorithm {

  protected MatrixAlgorithm m_PreFilter;

  protected AbstractMultiplicativeScatterCorrection m_Correction = new RangeBased();

  protected Matrix m_Average;

  @Override
  protected void doReset() {
    m_Average = null;
  }

  public void setPreFilter(MatrixAlgorithm value) {
    m_PreFilter = value;
  }

  public AbstractMultiplicativeScatterCorrection getCorrection() {
    return m_Correction;
  }

  public static Matrix createConfigurationMatrix(Matrix... spectra) {
    if (spectra == null || spectra.length == 0)
      throw new IllegalArgumentException("Can't create configuration matrix from 0 spectra");

    Matrix waveNumbers = spectra[0].getColumn(0);
    ArrayList<Matrix> amplitudes = new ArrayList<>();
    for (Matrix spectrum : spectra)
      amplitudes.add(spectrum.getColumn(1));

    return waveNumbers.concat(MatrixHelper.multiConcat(1, amplitudes.toArray(new Matrix[0])), 1);
  }

  @Override
  public void doConfigure(Matrix data) {
    Matrix waveNumbers = data.getColumn(0);
    Matrix amplitudes = data.getSubMatrix(0, data.numRows(), 1, data.numColumns());

    m_Average = waveNumbers.concatAlongColumns(amplitudes.mean(1));
  }

  @Override
  protected Matrix doTransform(Matrix data) {
    return m_Correction.correct(m_Average, data);
  }

  @Override
  public boolean isNonInvertible() {
    return true;
  }

  /**
   * Ancestor for schemes that perform multiplicative scatter correction.
   */
  public static abstract class AbstractMultiplicativeScatterCorrection {

    protected MatrixAlgorithm m_PreFilter;

    public abstract Matrix correct(Matrix average, Matrix data);
  }

  /**
   * Performs the correction using slopes/intercepts calculated for the defined ranges.
   */
  public static class RangeBased extends AbstractMultiplicativeScatterCorrection {

    protected List<double[]> m_Ranges = new ArrayList<>();

    public void addRange(double lower, double upper) {
      m_Ranges.add(new double[] { lower, upper });
    }

    @Override
    public Matrix correct(Matrix average, Matrix data) {
      Matrix result = data.copy();

      Matrix filtered;
      if (m_PreFilter != null)
        filtered = m_PreFilter.transform(data);
      else
        filtered = data;

      for (double[] range : m_Ranges) {
        ArrayList<Double> x = new ArrayList<>();
	ArrayList<Double> y = new ArrayList<>();
	ArrayList<Double> wave = new ArrayList<>();
	for (int i = 0; i < average.numRows(); i++) {
	  if (rangeContains(range, filtered.get(i, 0))) {
	    wave.add(filtered.get(i, 0));
	    y.add(filtered.get(i, 1));
	    x.add(average.get(i, 1));
	  }
	}

	double[] xArray = Utils.toDoubleArray(x.toArray(new Double[0]));
	double[] yArray = Utils.toDoubleArray(y.toArray(new Double[0]));
	double[] lr = Utils.linearRegression(xArray, yArray);
	double inter = lr[0];
	double slope = lr[1];

	for (int i = 0; i < average.numRows(); i++) {
	  if (rangeContains(range, result.get(i, 0))) {
	    result.set(i, 1, (result.get(i, 1) - inter) / slope);
	  }
	}
      }

      return result;
    }

    protected static boolean rangeContains(double[] range, double value) {
      double lower = range[0];
      double upper = range[1];

      return lower <= value && value <= upper;
    }
  }
}
