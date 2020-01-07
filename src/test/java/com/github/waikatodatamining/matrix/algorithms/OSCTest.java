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
 * OSCTest.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

import com.github.waikatodatamining.matrix.test.misc.TestRegression;

/**
 * Tests the OSC (Orthogonal Signal Correction) algorithm.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class OSCTest
  extends MatrixAlgorithmTest<OSC> {

  @Override
  protected OSC instantiateSubject() {
    return new OSC();
  }

  @TestRegression
  public void twoComponents() {
    m_subject.setNumComponents(2);
  }
}
