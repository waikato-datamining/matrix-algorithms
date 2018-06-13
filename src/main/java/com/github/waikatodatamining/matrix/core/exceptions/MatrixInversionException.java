package com.github.waikatodatamining.matrix.core.exceptions;

public class MatrixInversionException extends MatrixAlgorithmsException {
  private static final long serialVersionUID = -6704402669137006394L;

  private static final String PREFIX = "Could not invert matrix. ";

  public MatrixInversionException(String message) {
    super(PREFIX + message);
  }

  public MatrixInversionException(String message, Throwable cause) {
    super(PREFIX + message, cause);
  }
}
