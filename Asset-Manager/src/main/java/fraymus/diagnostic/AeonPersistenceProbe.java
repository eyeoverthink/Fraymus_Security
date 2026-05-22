package fraymus.diagnostic;

import fraymus.neural.AeonSingularity;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Headless proof harness for AEON Singularity persistence.
 *
 * Recruiter/public-demo value: this turns "memory persists" into a repeatable
 * filesystem check with exact byte counts, instead of a boot-log claim.
 */
public class AeonPersistenceProbe {
    private static final int DIMS = 8192;
    private static final long EXPECTED_BYTES = (long) DIMS * DIMS * Float.BYTES;
    private static final Path VAULT = Path.of("genesis_vault", "aeon_cortex.sys");

    public static void main(String[] args) throws Exception {
        System.out.println("FRAYMUS AEON PERSISTENCE PROBE");
        report("before");

        AeonSingularity engine = AeonSingularity.getInstance();
        System.out.println(engine.getStatus());
        engine.learn("fraymus phi harmonic persistence proof");
        engine.hibernate();

        report("after");
        long actual = Files.exists(VAULT) ? Files.size(VAULT) : -1;
        if (actual != EXPECTED_BYTES) {
            throw new IllegalStateException("Genesis vault size mismatch: expected "
                + EXPECTED_BYTES + " bytes, found " + actual);
        }
        System.out.println("PASS: Genesis vault exists and matches the AEON cortex tensor size.");
    }

    private static void report(String label) throws Exception {
        if (!Files.exists(VAULT)) {
            System.out.println(label + ": vault missing -> " + VAULT.toAbsolutePath());
            return;
        }
        System.out.println(label + ": vault=" + VAULT.toAbsolutePath()
            + " bytes=" + Files.size(VAULT)
            + " expected=" + EXPECTED_BYTES);
    }
}
