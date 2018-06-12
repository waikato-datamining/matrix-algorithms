package com.github.waikatodatamining.matrix.core.exceptions;

import com.github.waikatodatamining.matrix.core.Matrix;

import java.util.Arrays;

public class InvalidShapeException extends MatrixAlgorithmsException{

  private static final long serialVersionUID = 3673145587255476375L;

  public InvalidShapeException() {}

  public InvalidShapeException(String message) {
    super("Invalid shape " + message);
  }

  public InvalidShapeException(String message, Matrix... matrices) {
    super("Invalid shapes "
      + Arrays.stream(matrices).map(Matrix::shapeString).reduce((s, s2) -> s + ", " + s2).get()
      + " "
      + message);
  }
}
