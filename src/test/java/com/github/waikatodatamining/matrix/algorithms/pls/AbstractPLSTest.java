package com.github.waikatodatamining.matrix.algorithms.pls;

import com.github.waikatodatamining.matrix.algorithms.MatrixAlgorithmTest;
import com.github.waikatodatamining.matrix.core.matrix.Matrix;
import com.github.waikatodatamining.matrix.test.misc.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Abstract PLS Regression Test implementation.
 *
 * @param <T> AbstractPLS type
 * @author Steven Lang
 */
public abstract class AbstractPLSTest<T extends AbstractPLS> extends MatrixAlgorithmTest<T> {

  @Test
  public void checkTransformedNumComponents() throws Exception {
    Matrix X = m_inputData[0];
    Matrix Y = m_inputData[1];

    for (int i = 1; i < 5; i++) {
      m_subject.setNumComponents(i);
      m_subject.configure(X, Y);
      Matrix transform = m_subject.transform(X);
      Assertions.assertEquals(i, transform.numColumns());

      // Reset
      m_subject = instantiateSubject();
    }
  }

  /** Merge two tags with / if second tag is not empty */
  private static String mergeIfNotEmpty(String tag1, String tag2) {
    return tag1 + ("".equals(tag2) ? "" : "/" + tag2);
  }

  @Override
  protected void setupRegressions(T subject, Matrix[] inputData) {
    super.setupRegressions(subject, inputData);
    // Extract data
    Matrix X = inputData[0];
    
    addDefaultPlsMatrices(subject, X);
  }

  /** Adds default PLS matrices, that is predictions, transformations, loadings and model parameter matrices */
  protected void addDefaultPlsMatrices(AbstractPLS algorithm, Matrix x, String subTag) {
    // Add differences parameter and output matrices as regression check
    addPredictions(algorithm, x, subTag);
    addTransformation(algorithm, x, subTag);
    addLoadings(algorithm, subTag);
    addMatrices(algorithm, subTag);
  }

  /** Add transformation to the regression group */
  protected void addTransformation(AbstractPLS algorithm, Matrix x, String subTag) {
    addRegression(mergeIfNotEmpty(subTag, Tags.TRANSFORM), algorithm.transform(x));
  }

  /** Add predictions to the regression group */
  protected void addPredictions(AbstractPLS algorithm, Matrix x, String subTag)  {
    // Add predictions
    if (algorithm.canPredict()) {
      Matrix preds = algorithm.predict(x);
      addRegression(mergeIfNotEmpty(subTag, Tags.PREDICTIONS), preds);
    }
  }

  /** Add loadings to the regression group */
  protected void addLoadings(AbstractPLS algorithm, String subTag) {
    // Add loadings
    if (algorithm.hasLoadings()) {
      addRegression(mergeIfNotEmpty(subTag, Tags.LOADINGS), algorithm.getLoadings());
    }
  }

  /** Add model matrices to the regression group */
  protected void addMatrices(AbstractPLS algorithm, String subTag) {
    // Add matrices
    for (String matrixName : algorithm.getMatrixNames()) {
      String tag = Tags.MATRIX + "-" + matrixName;
      addRegression(mergeIfNotEmpty(subTag, tag), algorithm.getMatrix(matrixName));
    }
  }

  /** Adds default PLS matrices, that is predictions, transformations, loadings and model parameter matrices */
  protected void addDefaultPlsMatrices(T algorithm, Matrix x) {
    // Add differences parameter and output matrices as regression check
    addDefaultPlsMatrices(algorithm, x, "");
  }

  /** Add transformation to the regression group */
  protected void addTransformation(AbstractPLS algorithm, Matrix x) {
    addTransformation(algorithm, x, "");
  }

  /** Add predictions to the regression group */
  protected void addPredictions(AbstractPLS algorithm, Matrix x) {
    addPredictions(algorithm, x, "");
  }

  /** Add loadings to the regression group */
  protected void addLoadings(AbstractPLS algorithm) {
    addLoadings(algorithm, "");
  }

  /** Add model matrices to the regression group */
  protected void addMatrices(AbstractPLS algorithm) {
    addMatrices(algorithm, "");
  }
}
