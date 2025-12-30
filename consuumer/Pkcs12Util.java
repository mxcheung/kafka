import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class Pkcs12Util {

    private Pkcs12Util() {
        // Utility class; prevent instantiation
    }

    /**
     * Generates a PKCS#12 keystore file from PEM strings.
     *
     * @param certPem  PEM string of the certificate (can include intermediate certs)
     * @param keyPem   PEM string of the private key (PKCS#8)
     * @param alias    alias for the key entry
     * @param password keystore password
     * @param outputFilePath path to save the .p12 file
     * @throws Exception on failure
     */
    public static void generatePkcs12(String certPem, String keyPem, String alias, String password, String outputFilePath) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Convert PEM to Certificate(s)
        Certificate[] chain = certPem.lines()
                .filter(line -> !line.startsWith("-----"))
                .reduce("", (a, b) -> a + b)
                .getBytes(StandardCharsets.UTF_8);

        Certificate cert = cf.generateCertificate(new java.io.ByteArrayInputStream(Base64.getDecoder().decode(stripPemHeaders(certPem))));
        Certificate[] certChain = new Certificate[]{cert};

        // Convert PEM to PrivateKey
        byte[] keyBytes = Base64.getDecoder().decode(stripPemHeaders(keyPem));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        // Create PKCS12 keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, privateKey, password.toCharArray(), certChain);

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            ks.store(fos, password.toCharArray());
        }
    }

    private static String stripPemHeaders(String pem) {
        return pem.replaceAll("-----BEGIN (.*)-----", "")
                  .replaceAll("-----END (.*)-----", "")
                  .replaceAll("\\s", "");
    }
}
