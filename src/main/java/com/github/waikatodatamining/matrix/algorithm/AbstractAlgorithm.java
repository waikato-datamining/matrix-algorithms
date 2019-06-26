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
 * AbstractAlgorithm.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.LoggingObject;
import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Ancestor for algorithms.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractAlgorithm
  extends LoggingObject {

  private static final long serialVersionUID = 5147733768191663989L;

  /** whether the algorithm has been initialized. */
  protected boolean m_Initialized;

  /**
   * Returns whether the algorithm has been trained.
   *
   * @return		true if trained
   */
  public boolean isInitialized() {
    return m_Initialized;
  }


  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();
    m_Initialized = false;
  }

  /**
   * For outputting some information about the algorithm.
   *
   * @return		the information
   */
  public abstract String toString();
}
