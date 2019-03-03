package com.amazon.checkerframework.cryptopolicy.qual;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Annotation that gives the regexps for white listed crypto algorithms, modes, and padding.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({UnknownCryptoAlgorithm.class})
public @interface CryptoWhiteListed {
    /**
     * White listed algorithms as list of regular expressions
     * @return List of white-listed regexs.
     */
    String[] value() default {};

    /**
     * List of algorithms that should emit a warning even when white listed.
     * @return List of regexs that emit a warning
     */
    String[] warnOn() default {};
}
