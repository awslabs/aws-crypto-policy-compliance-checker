package com.amazon.checkerframework.cryptopolicy;

import java.util.LinkedHashSet;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.ValueChecker;

/**
 * Dummy class used by the checker framework to load the type factory.
 * Note that the CheckerFramework uses convention driven development by searching for a (x)Checker class and then
 * loading the (x)AnnotatedTypeFactory.
 * The checker does not provide a default @SuppressWarnings but uses
 * {@link com.amazon.checkerframework.cryptopolicy.qual.SuppressCryptoWarning} instead to
 * force users to get an exception for use of non-whitelisted ciphers.
 */
public class CryptoPolicyComplianceChecker extends BaseTypeChecker {

    @Override
    protected LinkedHashSet<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        LinkedHashSet<Class<? extends BaseTypeChecker>> checkers =
            super.getImmediateSubcheckerClasses();
        // run the value checker before this checker to propagate string constants around.
        checkers.add(ValueChecker.class);
        return checkers;
    }
}
