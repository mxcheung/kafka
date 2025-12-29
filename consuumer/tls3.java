import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class Tls {

    private Tls() {}

    // Example PEM literals (replace with your own or load from application.yml)
    public static final String CLIENT_CERT = """
        -----BEGIN CERTIFICATE-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr...
        -----END CERTIFICATE-----
        """;

    public static final String CLIENT_KEY = """
        -----BEGIN PRIVATE KEY-----
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQD...
        -----END PRIVATE KEY-----
        """;

    public static final String CA_CERT = """
        -----BEGIN CERTIFICATE-----
        MIIDdTCCAl2gAwIBAgIUFt...
        -----END CERTIFICATE-----
        """;

    public static void configureSSL() throws Exception {
        SSLContext sslContext = buildSSLContext(CLIENT_CERT, CLIENT_KEY, CA_CERT);

        // Set default so IBM MQ client (and Camel) uses it
        SSLContext.setDefault(sslContext);

        // Also create in-memory keystore/truststore and set system properties
        KeyStore keyStore = buildKeyStore(CLIENT_CERT, CLIENT_KEY);
        KeyStore trustStore = buildTrustStore(CA_CERT);

        // Write to temporary PKCS12/JKS files
        java.io.File keyFile = java.io.File.createTempFile("client-keystore", ".p12");
        keyFile.deleteOnExit();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(keyFile)) {
            keyStore.store(fos, "changeit".toCharArray());
        }

        java.io.File trustFile = java.io.File.createTempFile("truststore", ".jks");
        trustFile.deleteOnExit();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(trustFile)) {
            trustStore.store(fos, "changeit".toCharArray());
        }

        // System properties for IBM MQ + Camel
        System.setProperty("javax.net.ssl.keyStore", keyFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        System.setProperty("javax.net.ssl.trustStore", trustFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");

        // Enable full TLS debug
        System.setProperty("javax.net.debug", "ssl,handshake,trustmanager,keymanager");
    }

    private static SSLContext buildSSLContext(String certPem, String keyPem, String caPem) throws Exception {
        KeyStore ks = buildKeyStore(certPem, keyPem);
        KeyStore ts = buildTrustStore(caPem);

        javax.net.ssl.KeyManagerFactory kmf =
                javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        javax.net.ssl.TrustManagerFactory tmf =
                javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx;
    }

    private static KeyStore buildKeyStore(String certPem, String keyPem) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert;
        try (ByteArrayInputStream in = new ByteArrayInputStream(certPem.getBytes(StandardCharsets.US_ASCII))) {
            cert = (X509Certificate) cf.generateCertificate(in);
        }

        String base64Key = keyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64Key);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("client", privateKey, new char[0], new java.security.cert.Certificate[]{cert});
        return ks;
    }

    private static KeyStore buildTrustStore(String caPem) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;
        try (ByteArrayInputStream in = new ByteArrayInputStream(caPem.getBytes(StandardCharsets.US_ASCII))) {
            caCert = (X509Certificate) cf.generateCertificate(in);
        }

        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(null, null);
        ts.setCertificateEntry("ca", caCert);
        return ts;
    }
}
