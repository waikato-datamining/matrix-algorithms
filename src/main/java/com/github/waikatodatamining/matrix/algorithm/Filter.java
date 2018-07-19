package com.github.waikatodatamining.matrix.algorithm;

import com.github.waikatodatamining.matrix.core.Matrix;

public interface Filter {
  Matrix transform(Matrix predictors) throws Exception;
}
