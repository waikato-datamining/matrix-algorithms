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
 * Utils.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.waikatodatamining.matrix.core;

import java.lang.reflect.Array;

/**
 * Helper class, based on Weka's weka.core.Utils and ADAMS' adams.core.Utils.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Utils {

  public static final String NAN = "NaN";

  public static final String NEGATIVE_INFINITY = "-Infinity";

  public static final String POSITIVE_INFINITY = "+Infinity";

  /**
   * Initial index, filled with values from 0 to size - 1.
   */
  private static int[] initialIndex(int size) {

    int[] index = new int[size];
    for (int i = 0; i < size; i++) {
      index[i] = i;
    }
    return index;
  }

  /**
   * Sorts left, right, and center elements only, returns resulting center as
   * pivot.
   */
  private static int sortLeftRightAndCenter(double[] array, int[] index, int l,
    int r) {

    int c = (l + r) / 2;
    conditionalSwap(array, index, l, c);
    conditionalSwap(array, index, l, r);
    conditionalSwap(array, index, c, r);
    return c;
  }

  /**
   * Swaps two elements in the given integer array.
   */
  private static void swap(int[] index, int l, int r) {

    int help = index[l];
    index[l] = index[r];
    index[r] = help;
  }

  /**
   * Conditional swap for quick sort.
   */
  private static void conditionalSwap(double[] array, int[] index, int left,
    int right) {

    if (array[index[left]] > array[index[right]]) {
      int help = index[left];
      index[left] = index[right];
      index[right] = help;
    }
  }

  /**
   * Partitions the instances around a pivot. Used by quicksort and
   * kthSmallestValue.
   *
   * @param array the array of doubles to be sorted
   * @param index the index into the array of doubles
   * @param l the first index of the subset
   * @param r the last index of the subset
   *
   * @return the index of the middle element
   */
  private static int partition(double[] array, int[] index, int l, int r,
    double pivot) {

    r--;
    while (true) {
      while ((array[index[++l]] < pivot)) {
      }
      while ((array[index[--r]] > pivot)) {
      }
      if (l >= r) {
        return l;
      }
      swap(index, l, r);
    }
  }

  /**
   * Partitions the instances around a pivot. Used by quicksort and
   * kthSmallestValue.
   *
   * @param array the array of integers to be sorted
   * @param index the index into the array of integers
   * @param l the first index of the subset
   * @param r the last index of the subset
   *
   * @return the index of the middle element
   */
  private static int partition(int[] array, int[] index, int l, int r) {

    double pivot = array[index[(l + r) / 2]];
    int help;

    while (l < r) {
      while ((array[index[l]] < pivot) && (l < r)) {
        l++;
      }
      while ((array[index[r]] > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (array[index[r]] > pivot)) {
      r--;
    }

    return r;
  }

  /**
   * Implements quicksort with median-of-three method and explicit sort for
   * problems of size three or less.
   *
   * @param array the array of doubles to be sorted
   * @param index the index into the array of doubles
   * @param left the first index of the subset to be sorted
   * @param right the last index of the subset to be sorted
   */
  // @ requires 0 <= first && first <= right && right < array.length;
  // @ requires (\forall int i; 0 <= i && i < index.length; 0 <= index[i] &&
  // index[i] < array.length);
  // @ requires array != index;
  // assignable index;
  private static void quickSort(/* @non_null@ */double[] array, /* @non_null@ */
    int[] index, int left, int right) {

    int diff = right - left;

    switch (diff) {
    case 0:

      // No need to do anything
      return;
    case 1:

      // Swap two elements if necessary
      conditionalSwap(array, index, left, right);
      return;
    case 2:

      // Just need to sort three elements
      conditionalSwap(array, index, left, left + 1);
      conditionalSwap(array, index, left, right);
      conditionalSwap(array, index, left + 1, right);
      return;
    default:

      // Establish pivot
      int pivotLocation = sortLeftRightAndCenter(array, index, left, right);

      // Move pivot to the right, partition, and restore pivot
      swap(index, pivotLocation, right - 1);
      int center =
        partition(array, index, left, right, array[index[right - 1]]);
      swap(index, center, right - 1);

      // Sort recursively
      quickSort(array, index, left, center - 1);
      quickSort(array, index, center + 1, right);
    }
  }

  /**
   * Sorts a given array of doubles in ascending order and returns an array of
   * integers with the positions of the elements of the original array in the
   * sorted array. NOTE THESE CHANGES: the sort is no longer stable and it
   * doesn't use safe floating-point comparisons anymore. Occurrences of
   * Double.NaN are treated as Double.MAX_VALUE.
   *
   * @param array this array is not changed by the method!
   * @return an array of integers with the positions in the sorted array.
   */
  public static/* @pure@ */int[] sort(/* @non_null@ */double[] array) {

    int[] index = initialIndex(array.length);
    if (array.length > 1) {
      array = array.clone();
      quickSort(array, index, 0, array.length - 1);
    }
    return index;
  }

  /**
   * Computes the sum of the elements of an array of doubles.
   *
   * @param doubles the array of double
   * @return the sum of the elements
   */
  public static/* @pure@ */double sum(double[] doubles) {

    double sum = 0;

    for (double d : doubles) {
      sum += d;
    }
    return sum;
  }

  /**
   * Calculates the slope and intercept between the two arrays.
   *
   * @param x		the first array, representing the X values
   * @param y		the second array, representing the Y values
   * @return		intercept/slope
   */
  public static double[] linearRegression(double[] x, double[] y) {
    int n = x.length;
    double[] xTimesy = new double[n];
    for (int i = 0; i < n; i++)
      xTimesy[i] = x[i] * y[i];

    double a = (sum(y) * sumOfSquares(x) - sum(x) * sum(xTimesy))
               / (n * sumOfSquares(x) - Math.pow(sum(x), 2.0));

    double b = (n * sum(xTimesy) - sum(x) * sum(y))
               / (n * sumOfSquares(x) - Math.pow(sum(x), 2.0));

    return new double[] { a, b };
  }

  /**
   * Returns sum of the squares of all the elements in the array.
   *
   * @param doubles	the array to work on
   * @return		the sum
   */
  public static double sumOfSquares(double[] doubles) {
    double sum = 0.0;

    for (double d : doubles)
      sum += d * d;

    return sum;
  }

  /**
   * Turns the Number array into one consisting of primitive doubles.
   *
   * @param array	the array to convert
   * @return		the converted array
   */
  public static double[] toDoubleArray(Number[] array) {
    double[]	result;
    int		i;

    result = new double[array.length];
    for (i = 0; i < array.length; i++)
      result[i] = array[i].doubleValue();

    return result;
  }

  /**
   * Returns index of maximum element in a given array of doubles. First maximum
   * is returned.
   *
   * @param doubles the array of doubles
   * @return the index of the maximum element
   */
  public static/* @pure@ */int maxIndex(double[] doubles) {

    double maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < doubles.length; i++) {
      if ((i == 0) || (doubles[i] > maximum)) {
        maxIndex = i;
        maximum = doubles[i];
      }
    }

    return maxIndex;
  }

  /**
   * Rounds a double and converts it into String. Always displays the
   * specified number of decimals.
   *
   * @param value 		the double value
   * @param afterDecimalPoint 	the number of digits permitted
   * 				after the decimal point; if -1 then all
   * 				decimals are displayed; also if number > Long.MAX_VALUE
   * @return 			the double as a formatted string
   */
  public static String doubleToStringFixed(double value, int afterDecimalPoint) {
    StringBuilder	result;
    double		valueNew;
    double		factor;
    StringBuilder	remainder;
    char		separator;
    boolean		negative;

    // special numbers
    if (Double.isNaN(value)) {
      return NAN;
    }
    else if (Double.isInfinite(value)) {
      if (value < 0)
        return NEGATIVE_INFINITY;
      else
        return POSITIVE_INFINITY;
    }

    if (    (afterDecimalPoint < 0)
	 || (value > Long.MAX_VALUE)
	 || (value < Long.MIN_VALUE) ) {
      result = new StringBuilder("" + value);
    }
    else {
      negative  = (value < 0);
      if (negative)
	value *= -1.0;
      separator = '.';
      factor    = Math.pow(10, afterDecimalPoint);
      valueNew  = Math.floor(value * factor) / factor;
      result    = new StringBuilder(Long.toString(Math.round(Math.floor(valueNew))));
      remainder = new StringBuilder("" + Math.round((valueNew - Math.floor(valueNew)) * Math.pow(10, afterDecimalPoint)));
      remainder.delete(0, remainder.indexOf("" + separator) + 1);
      if (afterDecimalPoint > 0) {
	while (remainder.length() < afterDecimalPoint)
	  remainder.insert(0, '0');
	result.append(separator);
	result.append(remainder.substring(0, afterDecimalPoint));
      }
      if (negative && (valueNew != 0.0))
	result.insert(0, "-");
    }

    return result.toString();
  }

  /**
   * Returns the dimensions of the given array. Even though the
   * parameter is of type "Object" one can hand over primitve arrays, e.g.
   * int[3] or double[2][4].
   *
   * @param array       the array to determine the dimensions for
   * @return            the dimensions of the array
   */
  public static int getArrayDimensions(Class array) {
    if (array.getComponentType().isArray())
      return 1 + getArrayDimensions(array.getComponentType());
    else
      return 1;
  }

  /**
   * Returns the dimensions of the given array. Even though the
   * parameter is of type "Object" one can hand over primitve arrays, e.g.
   * int[3] or double[2][4].
   *
   * @param array       the array to determine the dimensions for
   * @return            the dimensions of the array
   */
  public static int getArrayDimensions(Object array) {
    return getArrayDimensions(array.getClass());
  }

  /**
   * Returns the given Array in a string representation. Even though the
   * parameter is of type "Object" one can hand over primitve arrays, e.g.
   * int[3] or double[2][4].
   *
   * @param array       the array to return in a string representation
   * @param outputClass	whether to output the class name instead of calling
   * 			the object's "toString()" method
   * @return            the array as string
   */
  public static String arrayToString(Object array, boolean outputClass) {
    StringBuilder	result;
    int			dimensions;
    int			i;
    Object		obj;

    result     = new StringBuilder();
    dimensions = getArrayDimensions(array);

    if (dimensions == 0) {
      result.append("null");
    }
    else if (dimensions == 1) {
      for (i = 0; i < Array.getLength(array); i++) {
	if (i > 0)
	  result.append(",");
	if (Array.get(array, i) == null) {
	  result.append("null");
	}
	else {
	  obj = Array.get(array, i);
	  if (outputClass) {
	    if (obj instanceof Class)
	      result.append(((Class) obj).getName());
	    else
	      result.append(obj.getClass().getName());
	  }
	  else {
	    result.append(obj.toString());
	  }
	}
      }
    }
    else {
      for (i = 0; i < Array.getLength(array); i++) {
	if (i > 0)
	  result.append(",");
	result.append("[" + arrayToString(Array.get(array, i)) + "]");
      }
    }

    return result.toString();
  }

  /**
   * Returns the given Array in a string representation. Even though the
   * parameter is of type "Object" one can hand over primitve arrays, e.g.
   * int[3] or double[2][4].
   *
   * @param array       the array to return in a string representation
   * @return            the array as string
   */
  public static String arrayToString(Object array) {
    return arrayToString(array, false);
  }

  /**
   * Normalises a value against a given mean/standard deviation.
   *
   * @param value   The value to normalise.
   * @param mean    The mean of the distribution.
   * @param stdDev  The standard deviation of the distribution.
   * @return        The normalised value.
   */
  public static double normalise(double value, double mean, double stdDev) {
    // Avoid divide-by-zero error
    if (stdDev == 0.0)
      return value - mean;

    return (value - mean) / stdDev;
  }
}
