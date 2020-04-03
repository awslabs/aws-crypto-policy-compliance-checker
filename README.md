## CheckerFramework type system for Crypto Compliance

A Java compiler plugin that checks that no weak cipher algorithms are used with the Java crypto API.

### How does it work?

The checker builds on the Checker Framework (www.checkerframework.org), an open-source tool for building extensions to
the Java compiler's typechecker. A typechecker is perfect for checking a compliance rule, because typecheckers are
*sound*, meaning that they never miss errors, but might report false positives. In other words, a typechecker
over-approximates what your program might do at runtime, so if the checker reports that the code is safe, you can be
confident that it is. If the checker issues an error, there are two possibilities:

1. there is a real issue: you are using a crypto algorithm that is not whitelisted in the stubs folder, OR
2. the type system cannot understand what algorithm you are trying to use. This happens if you load the algorithm
from a file, or wrap it in enums. Complicated code like this is usually discouraged, but you can add an exception as
described in the section below.

## How do I run it?

The CheckerFramework provides different build system integrations that are described on their wiki. For a quick start, try running it on a single file:

```
./gradlew assemble
./gradlew copyDependencies

javac -cp ./build/libs/aws-crypto-policy-compliance-checker.jar:dependencies/checker-3.1.1.jar -processor com.amazon.checkerframework.cryptopolicy.CryptoPolicyComplianceChecker -AstubDebug -Astubs=stubs/java.security.astub:stubs/javax.crypto.astub tests/crypto-policy/CryptoTests.java
```

The output should read sth like:

```
Note: StubParser: parsing stub file statically-executable.astub
Note: StubParser: parsing stub file /Users/schaef/workspace/crypto/src/CryptoPolicyComplianceChecker/stubs/java.security.astub
Note: StubParser: parsing stub file /Users/schaef/workspace/crypto/src/CryptoPolicyComplianceChecker/stubs/javax.crypto.astub
warning: You do not seem to be using the distributed annotated JDK.  To fix the problem, supply javac an argument like:  -Xbootclasspath/p:.../checker/dist/ .  Currently using: jdk8.jar
tests/crypto-policy/CryptoTests.java:39: error: [bad.crypto.issue.url] Please provide a valid URL that justifies the use of algorithm DES.
    static void suppressedWarningsWithBadURL() throws Exception {
                ^
  	badurl
  is not a valid URL.
tests/crypto-policy/CryptoTests.java:23: error: [crypto.policy.violation] Used crypto algorithm: DEFAULT/PADDING is not strong enough. Consider using a stronger algorithm such as RSA/ECB/OAEPPadding.
        Cipher.getInstance("Default/padding", "fake provider");
                           ^
tests/crypto-policy/CryptoTests.java:26: error: [crypto.policy.violation] Used crypto algorithm: DES is not strong enough. Consider using a stronger algorithm such as RSA/ECB/OAEPPadding.
        Cipher.getInstance("des");
                           ^
tests/crypto-policy/CryptoTests.java:29: error: [crypto.policy.violation] Used crypto algorithm: DES is not strong enough. Consider using a stronger algorithm such as RSA/ECB/OAEPPadding.
        Cipher.getInstance(ENCRYPTION_ALGORITHM);
                           ^
4 errors
1 warning

```

The first few lines are emitted by the `-AstubDebug`, which you can omit if you are certain that your stubs are
correct.
You can add more stub files to the `-Astubs` parameter as needed.

## What Ciphers are approved
The list of approved ciphers can be found as annotations in the `stubs` folder. The stub files are only an example.
Strengthen or weaken the white-list according to the policy or compliance regime that you want to follow.
Remember to adjust the warning messages in `src/main/resources/com/amazon/checkerframework/cryptopolicy/messages.properties`
accordingly (not that messages in this file have to be in a single line and line breaks have to be encoded as `\n`.

Learn more on how to write stub files
from the [CheckerFramework documentation](https://checkerframework.org/manual/#stub). We use the
`@CryptoWhiteListed` annotation which takes an array of regular expressions as argument. E.g.

```
@CryptoWhiteListed(value={"HmacSHA-?(1.*|2.*|384|512.*)"}) String arg
```

To white-list all values that match HmacSHA-1.*, HmacSHA-2.*, etc, or

```
@CryptoWhiteListed(value={"HmacSHA-?(1.*|2.*|384|512.*)"}, warnOn={"HmacSHA-?1"}) String arg
```

To white-list the same set of string values, but emit a compiler warning for HmacSHA1 and HmacSHA-1.
Any algorithm passed as `arg` that is not part of the white-list will cause an error.

### How do I add an exception?

If you use a non-whitelisted algorithm and you are sure that it is safe, you can add
a `@SuppressCryptoWarning(issue = "url-to-issue")` to your code. Note that the `url-to-issue` has to be
a valid URL to a issue where you got approval to use the algorithm.


### Whitelisted Algorithms
The white-list in this repository is only an example. We do not suggest that all algorithms in the list a strong, or
that other are not. Also, the stub files only cover Java crypto classes. If you want to use this type system with
other crypto libraries such as bouncy castle, you will have to extend the list.
