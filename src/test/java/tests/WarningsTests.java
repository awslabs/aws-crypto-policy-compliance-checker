import java.io.File;
import java.util.List;

import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test runner that uses the Checker Framework's tooling.
 */
public class WarningsTests extends CheckerFrameworkPerDirectoryTest {

    private static final String TEST_DATA_SUBDIR_NAME = "crypto-warnings";

    public WarningsTests(List<File> testFiles) {
        super(
            testFiles,
            com.amazon.checkerframework.cryptopolicy.CryptoPolicyComplianceChecker.class,
            TEST_DATA_SUBDIR_NAME,
            "-Anomsgtext",  // don't print error text, just the key.
            "-Anocheckjdk",
            "-nowarn",
            "-Astubs=stubs");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[]{TEST_DATA_SUBDIR_NAME};
    }
}