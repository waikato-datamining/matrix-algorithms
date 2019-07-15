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
 * DownsampleTest.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.filter;

import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import com.github.waikatodatamining.matrix.test.misc.TestDataset;

/**
 * Tests the Downsample class.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class DownsampleTest
  extends AbstractRegressionTest<Downsample> {

  @Override
  protected void setupRegressions(Downsample subject, Matrix[] inputData) throws Exception {
    Matrix X = inputData[0];

    Matrix filtered = subject.transform(X);

    addRegression(Tags.TRANSFORM, filtered);
  }

  @Override
  protected TestDataset[] getDatasets() {
    return new TestDataset[]{
      TestDataset.BOLTS
    };
  }

  @Override
  protected Downsample instantiateSubject() {
    Downsample subject = new Downsample();
    subject.setStartIndex(3);
    subject.setStep(4);
    return subject;
  }
}
