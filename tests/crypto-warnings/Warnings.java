import java.security.MessageDigest;
import javax.crypto.KeyAgreement;

public class Warnings {

    public static void  example01() throws Exception {
        // :: warning: (crypto.policy.warning)
        MessageDigest.getInstance("md5");
    }
}
