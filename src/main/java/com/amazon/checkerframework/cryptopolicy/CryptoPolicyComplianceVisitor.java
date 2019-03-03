package com.amazon.checkerframework.cryptopolicy;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazon.checkerframework.cryptopolicy.qual.CryptoBlackListed;
import com.amazon.checkerframework.cryptopolicy.qual.CryptoWhiteListed;
import com.amazon.checkerframework.cryptopolicy.qual.SuppressCryptoWarning;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueAnnotatedTypeFactory;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.value.qual.StringVal;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Modifies the common assignment check to permit values with StringVal annotations from
 * the Value Checker to be assigned to lhs_s that require a crypto whitelist.
 */
public class CryptoPolicyComplianceVisitor extends BaseTypeVisitor {

    private static final @CompilerMessageKey String CRYPTO_COMPLIANCE_WARNING_KEY = "crypto.policy.warning";
    private static final @CompilerMessageKey String CRYPTO_COMPLIANCE_ERROR_KEY = "crypto.policy.violation";
    private static final @CompilerMessageKey String BAD_URL_KEY = "bad.crypto.issue.url";
    private static final @CompilerMessageKey String UNKNOWN_ALGORITHM_KEY = "crypto.cipher.unknown";

    /**
     * Default constructor.
     *
     * @param checker Provided by the checker framework
     */
    public CryptoPolicyComplianceVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    /**
     * Changes how assignments and pseudo-assignments are resolved. lhsType is the type of the lhs expression,
     * rhsTree is the rhs of the assignment.
     *
     * @param lhsType  lhs type of the assignment
     * @param rhsTree  rhs tree of the assingment
     * @param errorKey key of the error to issue if this check fails.
     */
    @Override
    public void commonAssignmentCheck(final AnnotatedTypeMirror lhsType,
                                      final ExpressionTree rhsTree,
                                      final String errorKey) {

        final List<String> stringValAnnotations = getLowerCasedStringValAnnotations(rhsTree);
        final AnnotationMirror whiteListAnno = lhsType.getAnnotation(CryptoWhiteListed.class);
        final AnnotationMirror blackListAnno = lhsType.getAnnotation(CryptoBlackListed.class);

        // If the lhs isn't a Crypto Policy whitelist or blacklist, or if the rhs does not have
        // any StringVal annotations then there is nothing to do.
        if ((whiteListAnno == null && blackListAnno == null)) {
            super.commonAssignmentCheck(lhsType, rhsTree, errorKey);
            return;
        }
        // If we cannot determine what algorithm is used we fail the build as well to avoid false negatives.
        if (stringValAnnotations == null || stringValAnnotations.isEmpty()) {
            checker.report(Result.failure(UNKNOWN_ALGORITHM_KEY), rhsTree);
            return;
        }

        final Set<String> disallowedCiphers = new HashSet<>();
        final Set<String> warningCiphers = new HashSet<>();
        if (whiteListAnno != null) {
            List<String> regexList = AnnotationUtils.getElementValueArray(whiteListAnno, "value", String.class, true);
            disallowedCiphers.addAll(matchCiphersFromAnnotation(regexList, stringValAnnotations, false));

            List<String> warnList = AnnotationUtils.getElementValueArray(whiteListAnno, "warnOn", String.class, true);
            warningCiphers.addAll(matchCiphersFromAnnotation(warnList, stringValAnnotations, true));
        }

        if (blackListAnno != null) {
            List<String> regexList = AnnotationUtils.getElementValueArray(blackListAnno, "value", String.class, true);
            disallowedCiphers.addAll(matchCiphersFromAnnotation(regexList, stringValAnnotations, true));
        }

        // remove all disallowedCiphers from the warningCiphers because we report an error about those already.
        warningCiphers.removeAll(disallowedCiphers);
        if (!warningCiphers.isEmpty()) {
            final String messageString = String.join(", ", warningCiphers).toUpperCase();
            if (!shouldSuppressWarnings(rhsTree, messageString)) {
                checker.report(Result.warning(CRYPTO_COMPLIANCE_WARNING_KEY, messageString), rhsTree);
            }
        }

        // if none of the regex checks returned false, then we can skip the rest of the CAC
        if (!disallowedCiphers.isEmpty()) {
            final String messageString = String.join(", ", disallowedCiphers).toUpperCase();
            if (!shouldSuppressWarnings(rhsTree, messageString)) {
                checker.report(Result.failure(CRYPTO_COMPLIANCE_ERROR_KEY, messageString), rhsTree);
            }
        }
    }

    /**
     * Find the sub list of stringList that match regular expressions in the cipher annotation.
     * Note that for simplicity, all regex strings are lower-cased since the standard says crypto algorithms
     * are not case-sensitive.
     * Set positive to get the sublist of things that do not match.
     *
     * @param regexList  List of regex that should be matched
     * @param stringList The list that should be matched.
     * @param positive   True, to retain all matches, False to retain strings that don't have a match.
     * @return Sublist of strings.
     */
    private List<String> matchCiphersFromAnnotation(final List<String> regexList,
                                                    final List<String> stringList,
                                                    final boolean positive) {
        // if there are no valid whitelist items, don't continue
        if (regexList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Boolean> valuesMatched =
            stringList.stream()
                      .map(value -> regexList.stream()
                                             .anyMatch(regex -> value.matches(regex.toLowerCase())))
                      .collect(Collectors.toList());

        final List<String> matchedCiphers = new ArrayList<>();
        for (int i = 0; i < valuesMatched.size(); i++) {
            if (valuesMatched.get(i) == positive) {
                matchedCiphers.add(stringList.get(i));
            }
        }
        return matchedCiphers;
    }

    private List<String> getLowerCasedStringValAnnotations(final ExpressionTree expressionTree) {
        // get the actual Strings that the rhs can resolve to
        ValueAnnotatedTypeFactory valueAnnotatedTypeFactory =
            (ValueAnnotatedTypeFactory) atypeFactory.getTypeFactoryOfSubchecker(ValueChecker.class);

        AnnotatedTypeMirror valueType = valueAnnotatedTypeFactory.getAnnotatedType(expressionTree);
        AnnotationMirror stringValAnno = valueType.getAnnotation(StringVal.class);

        // if the rhs doesn't have any constant-time strings that it can resolve to, give up
        if (stringValAnno == null) {
            return new ArrayList<>();
        }
        return AnnotationUtils.getElementValueArray(stringValAnno, "value", String.class, true)
                              .stream()
                              .map(String::toLowerCase)
                              .collect(Collectors.toList());
    }


    private boolean shouldSuppressWarnings(final ExpressionTree tree, final String suppressedString) {
        final TreePath path = trees.getPath(this.root, tree);
        if (path != null) {
            final VariableTree var = TreeUtils.enclosingVariable(path);
            if (var != null && hasSuppresssCryptoAnnotation(TreeUtils.elementFromTree(var),
                                                            suppressedString)) {
                return true;
            }
            final MethodTree method = TreeUtils.enclosingMethod(path);
            if (method != null) {
                final Element elt = TreeUtils.elementFromTree(method);

                return hasSuppresssCryptoAnnotation(elt, suppressedString);
            }
            final ClassTree cls = TreeUtils.enclosingClass(path);
            if (cls != null) {
                final Element elt = TreeUtils.elementFromTree(cls);
                return hasSuppresssCryptoAnnotation(elt, suppressedString);
            }
        }
        return false;
    }

    private boolean hasSuppresssCryptoAnnotation(final Element elt, final String suppressedString) {
        final SuppressCryptoWarning anno = elt.getAnnotation(SuppressCryptoWarning.class);
        if (anno != null) {
            // Code to validate that the string in the annotation is a valid URL. In theory, we
            // would like to enforce that this URL also refers to a issue that gives this package
            // an exception to use the relevant algorithm, but this is not feasible to implement.
            // Mostly because we won't have network access during fleet builds to perform any sort
            // of validation. Instead, we just check if the string is a valid URL to deter users
            // from cheating by putting in empty string, etc.
            try {
                final URL issueUrl = new URL(anno.issue());
                System.out.println("Suppressing warning for "
                                   + suppressedString
                                   + " is approved by "
                                   + issueUrl.toString());
            } catch (MalformedURLException e) {
                checker.report(Result.failure(BAD_URL_KEY, suppressedString, anno.issue()), elt);
            }
            return true;
        }
        return false;
    }
}
