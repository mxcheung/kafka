import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PemToP12 {

    public static void main(String[] args) throws Exception {
        String certPath = "cert.pem";
        String keyPath = "key.pem";
        String p12Path = "keystore.p12";
        String password = "changeit";

        // --- Load certificate ---
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(Files.newInputStream(Path.of(certPath)));

        // --- Load private key ---
        String keyPem = Files.readString(Path.of(keyPath))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyPem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        // --- Create PKCS12 keystore ---
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("myalias", privateKey, password.toCharArray(), new Certificate[]{cert});

        // --- Write to file ---
        try (FileOutputStream fos = new FileOutputStream(p12Path)) {
            ks.store(fos, password.toCharArray());
        }

        System.out.println("PKCS12 file created at " + p12Path);
    }
}
