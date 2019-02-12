package com.amazon.checkerframework.cryptopolicy.qual;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Annotation that gives the regexps for crypto algorithms, modes, and padding that are
 * white-listed, but result in a compiler warning.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({UnknownCryptoAlgorithm.class})
public @interface CryptoWarnings {
    /**
     * List of algorithms as list of regular expressions that should emit a warning
     * @return List of regexps that will result in a warning.
     */
    String[] value() default {};
}
