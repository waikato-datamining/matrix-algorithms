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
 * RowNormTest.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.transformation;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Tests the RowNorm transformation.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class RowNormTest extends AbstractTransformationTest<RowNorm> {

  @Test
  public void checkZeroVariance() {
    Matrix X = m_inputData[0];

    Matrix transform = m_subject.transform(X);


    double actualMean = transform.mean();
    double expectedMean = 0.0;
    double expectedStd = 1.0;

    Assertions.assertEquals(expectedMean, actualMean, 1e-7);

    for (int i = 0; i < transform.numColumns(); i++) {
      double actualStd = MatrixHelper.stdev(transform, i, false);
      Assertions.assertEquals(expectedStd, actualStd, 1e-7);
    }
  }

  @Override
  protected RowNorm instantiateSubject() {
    return new RowNorm();
  }

  @Test
  @Disabled("RowNorm now reconfigures on every input," +
    " so inverse transform does not behave as expected")
  @Override
  public void checkInvTransformEqInput() {
    super.checkInvTransformEqInput();
  }
}