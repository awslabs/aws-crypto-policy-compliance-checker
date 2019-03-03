import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import com.amazon.checkerframework.cryptopolicy.qual.SuppressCryptoWarning;

public class CryptoTests {

    private static final String BAD_CIPHER = "des";

    public static final String ENCRYPTION_ALGORITHM = "DES";
    public static final String FULL_ENCRYPTION_ALGORITHM = "DES/CBC/PKCS5Padding";
    public static final String SALT = "imdbrulz";

    static void test() throws Exception {
        Cipher.getInstance("RSA/ECB/OAEPPadding", "with provider");

        // :: error: (crypto.policy.violation)
        Cipher.getInstance("Default/padding", "fake provider");

        // :: error: (crypto.policy.violation)
        Cipher.getInstance("des");

        // :: error: (crypto.policy.violation)
        Cipher.getInstance(ENCRYPTION_ALGORITHM);

        // :: error: (crypto.policy.violation)
        MessageDigest.getInstance(BAD_CIPHER);
    }

    @SuppressCryptoWarning(issue = "https://github.com/myteam/myproject/issues/123")
    static void suppressedWarnings() throws Exception {
        Cipher.getInstance("des");
    }

    @SuppressCryptoWarning(issue = "badurl")
    // :: error: (bad.crypto.issue.url)
    static void suppressedWarningsWithBadURL() throws Exception {
        Cipher.getInstance("des");
    }

    // From IMDbAdvertisingSite for testing SuppressCryptoWarning
    @SuppressCryptoWarning(issue = "https://someurl.com")
    static void moreComplexSuppressWarnings() {
        byte[] secretBytes = SALT.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secretBytes, ENCRYPTION_ALGORITHM);
        byte[] decryptedBytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(FULL_ENCRYPTION_ALGORITHM);
            if (cipher != null) {
                // some code
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // some code
        }
    }

}
