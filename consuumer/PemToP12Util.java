import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PemToP12Util {

    /**
     * Convert PEM strings to PKCS#12 keystore bytes.
     *
     * @param certPem      Certificate PEM string
     * @param keyPem       Private key PEM string (PKCS#8)
     * @param chainPem     Optional certificate chain PEMs (can be null)
     * @param alias        Key alias
     * @param password     PKCS#12 password
     * @return byte[] of PKCS#12 keystore
     * @throws Exception
     */
    public static byte[] convertToP12(String certPem, String keyPem, String chainPem,
                                     String alias, String password) throws Exception {

        // --- Load certificate ---
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(
                new java.io.ByteArrayInputStream(certPem.getBytes()));

        // --- Load private key ---
        String privateKeyPEM = keyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        // --- Handle optional chain ---
        Certificate[] chain;
        if (chainPem != null && !chainPem.isBlank()) {
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(chainPem.getBytes());
            chain = cf.generateCertificates(bis).toArray(new Certificate[0]);
        } else {
            chain = new Certificate[]{cert};
        }

        // --- Create PKCS12 keystore in memory ---
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, privateKey, password.toCharArray(), chain);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ks.store(baos, password.toCharArray());
            return baos.toByteArray();
        }
    }

    // --- Example usage ---
    public static void main(String[] args) throws Exception {
        final String CERT_PEM = "-----BEGIN CERTIFICATE-----\nMIID...==\n-----END CERTIFICATE-----";
        final String KEY_PEM = "-----BEGIN PRIVATE KEY-----\nMIIE...==\n-----END PRIVATE KEY-----";
        final String CHAIN_PEM = null; // optional

        byte[] p12Bytes = convertToP12(CERT_PEM, KEY_PEM, CHAIN_PEM, "myalias", "changeit");

        // Write to file (optional)
        java.nio.file.Files.write(java.nio.file.Path.of("keystore.p12"), p12Bytes);

        System.out.println("PKCS12 generated in memory and saved to keystore.p12");
    }
}
