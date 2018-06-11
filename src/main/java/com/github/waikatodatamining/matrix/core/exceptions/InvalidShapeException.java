package com.github.waikatodatamining.matrix.core.exceptions;

public class InvalidShapeException extends MatrixAlgorithmsException{

  private static final long serialVersionUID = 3673145587255476375L;

  public InvalidShapeException() {}

  public InvalidShapeException(String message) {
    super("Invalid shape " + message);
  }
}
