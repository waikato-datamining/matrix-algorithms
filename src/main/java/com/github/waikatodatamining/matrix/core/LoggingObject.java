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
 * LoggingObject.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Ancestor for objects with logging support.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class LoggingObject
  implements Serializable {

  private static final long serialVersionUID = -8620918197044300603L;

  /** The logger to use. */
  private transient Logger m_Logger;

  /** Whether to output debug information. */
  protected boolean m_Debug;

  /**
   * Sets whether to output debugging information.
   *
   * @param value	true if to output debugging information
   */
  public void setDebug(boolean value) {
    m_Debug = value;
  }

  /**
   * Returns whether to output debugging information.
   *
   * @return		true if to output debugging information
   */
  public boolean getDebug() {
    return m_Debug;
  }

  /**
   * Returns the logger.
   *
   * @return		the logger
   */
  public synchronized Logger getLogger() {
    // Lazy initialisation
    if (m_Logger == null)
      m_Logger = Logger.getLogger(getClass().getName());

    return m_Logger;
  }
}
