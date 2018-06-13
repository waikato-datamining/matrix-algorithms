package com.github.waikatodatamining.matrix.core.exceptions;

public class MatrixAlgorithmsException extends RuntimeException {
  private static final long serialVersionUID = -3583358235821258766L;

  public MatrixAlgorithmsException() {
  }

  public MatrixAlgorithmsException(String message) {
    super(message);
  }

  public MatrixAlgorithmsException(String message, Throwable cause) {
    super(message, cause);
  }

  public MatrixAlgorithmsException(Throwable cause) {
    super(cause);
  }

  public MatrixAlgorithmsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
