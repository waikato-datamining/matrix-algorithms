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
 * SavitzkyGolay2.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithms;

/**
 * SavitzkyGolay2 just ensures that SavitzkyGolay transformation has
 * the same number of points to the left and right.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class SavitzkyGolay2 extends SavitzkyGolay {

  protected int m_NumPoints = 3;

  @Override
  public void setNumPointsLeft(int value) {
    setNumPoints(value);
  }

  @Override
  public void setNumPointsRight(int value) {
    setNumPoints(value);
  }

  public void setNumPoints(int value) {
    if (value < 0)
      throw new IllegalArgumentException("Number of points must be at least 0");

    m_NumPoints = m_NumPointsLeft = m_NumPointsRight = value;
  }

}
