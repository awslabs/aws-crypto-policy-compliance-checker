package com.amazon.checkerframework.cryptopolicy.qual;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Annotation that gives the regexps for black-listed crypto algorithms.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({UnknownCryptoAlgorithm.class})
public @interface CryptoBlackListed {
    /**
     * Black listed algorithms as list of regular expressions. Default is that if an empty black list annotation is
     * present, reject all calls - so @CryptoBlackListed String means that no values are permitted.
     * @return List of black-listed regexps.
     */
    String[] value() default {".*"};
}
