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
 * SupervisedFilter.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core.api;

import com.github.waikatodatamining.matrix.core.Matrix;

/**
 * Interface for filters which are supervised, and therefore require
 * training before their transform method can be used.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public interface SupervisedFilter extends Filter {

  /**
   * Initializes using the provided data.
   *
   * @param predictors	the input data
   * @param response 	the dependent variable(s)
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  String initialize(Matrix predictors, Matrix response) throws Exception;
}
