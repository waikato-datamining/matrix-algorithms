package com.github.waikatodatamining.matrix.core.exceptions;

public class InvalidAxisException extends MatrixAlgorithmsException{

  private static final long serialVersionUID = 4764218064137525033L;

  public InvalidAxisException(int axis) {
    super("Axis has to be either 0 or 1 but was " + axis + ".");
  }
}
