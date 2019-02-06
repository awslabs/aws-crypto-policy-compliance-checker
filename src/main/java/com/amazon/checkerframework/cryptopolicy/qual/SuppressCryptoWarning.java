package com.amazon.checkerframework.cryptopolicy.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation to suppress crypto warnings if a relevant issue it provided
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@SubtypeOf({})
public @interface SuppressCryptoWarning {
    /**
     * The issue URL (e.g. github or jira) where the suppress warning was discussed with security.
     * @return URL of an issue.
     */
    String issue();
}
