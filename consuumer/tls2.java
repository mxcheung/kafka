import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Base64;

public final class Tls {

    
    // ---------------- PEM constants (text blocks, no concatenation) ----------------
    private static final String CLIENT_CERT = """
        -----BEGIN CERTIFICATE-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr...
        ...rest of base64...
        -----END CERTIFICATE-----
        """;

    private static final String CLIENT_KEY = """
        -----BEGIN PRIVATE KEY-----
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQD...
        ...rest of base64...
        -----END PRIVATE KEY-----
        """;

    private static final String CA_CERT = """
        -----BEGIN CERTIFICATE-----
        MIIDdTCCAl2gAwIBAgIUFt...
        ...rest of base64...
        -----END CERTIFICATE-----
        """;
    
    public static SSLContext sslContextFromEnv() throws Exception {

        String certPem = requireEnv("CLIENT_CERT");
        String keyPem  = requireEnv("CLIENT_KEY");
        String caPem   = requireEnv("CA_CERT");

        // ---- certificates ----
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate clientCert = readCertificate(cf, certPem);
        X509Certificate caCert     = readCertificate(cf, caPem);

        // ---- private key (PKCS#8, unencrypted) ----
        PrivateKey privateKey = readPrivateKey(keyPem);

        // ---- key store (in-memory) ----
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(
                "client",
                privateKey,
                new char[0],
                new Certificate[]{clientCert}
        );

        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        // ---- trust store ----
        KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        ts.load(null, null);
        ts.setCertificateEntry("ca", caCert);

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // ---- SSLContext ----
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx;
    }

    // ---------- helpers ----------

    private static String requireEnv(String name) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return v;
    }

    private static X509Certificate readCertificate(
            CertificateFactory cf, String pem) throws CertificateException {

        try (InputStream in =
                     new ByteArrayInputStream(pem.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    private static PrivateKey readPrivateKey(String pem) throws Exception {
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(base64);

        // RSA key (change to "EC" if required)
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
    }
}
