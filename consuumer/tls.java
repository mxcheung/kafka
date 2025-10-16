import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import org.apache.hc.core5.ssl.SSLContexts;

public class MtlsConfig {

    public static SSLContext createSslContextFromEnv() throws Exception {
        String certPem = System.getenv("CLIENT_CERT");
        String keyPem = System.getenv("CLIENT_KEY");

        if (certPem == null || keyPem == null) {
            throw new IllegalStateException("CLIENT_CERT or CLIENT_KEY environment variable not set");
        }

        // --- clean and decode private key ---
        String keyClean = keyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyClean);
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        // --- parse certificate ---
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)
                cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));

        // --- build in-memory PKCS12 keystore ---
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("alias", privateKey, "changeit".toCharArray(),
                new java.security.cert.Certificate[]{cert});

        // --- create SSLContext for mutual TLS ---
        return SSLContexts.custom()
                .loadKeyMaterial(keyStore, "changeit".toCharArray())
                .build();
    }
}