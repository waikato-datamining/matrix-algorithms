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
 * MultiFilter.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.filter;

import com.github.waikatodatamining.matrix.core.LoggingObject;
import com.github.waikatodatamining.matrix.core.Matrix;
import com.github.waikatodatamining.matrix.core.api.Filter;

import java.util.List;

/**
 * Filter which encapsulates a series of sub-filters, and applies
 * them in a given order.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class MultiFilter
  extends LoggingObject
  implements Filter {

  // The filters in order of application
  protected List<Filter> m_Filters;

  public MultiFilter(List<Filter> filters) {
    m_Filters = filters;
  }

  @Override
  public Matrix transform(Matrix predictors) throws Exception {
    // The result starts as the predictors
    Matrix result = predictors;

    // Apply each filter in ordered turn
    for (Filter filter : m_Filters)
      result = filter.transform(result);

    return result;
  }
}
