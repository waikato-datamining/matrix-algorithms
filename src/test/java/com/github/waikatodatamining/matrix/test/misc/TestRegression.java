package com.github.waikatodatamining.matrix.test.misc;

import com.github.waikatodatamining.matrix.test.AbstractRegressionTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Test annotation that will add the
 * {@link AbstractRegressionTest#REGRESSION_TAG} tag to the executed method.
 *
 * @author Steven Lang
 */
@Target({METHOD})
@Retention(RUNTIME)
@Test
@Tag(AbstractRegressionTest.REGRESSION_TAG)
public @interface TestRegression {

}