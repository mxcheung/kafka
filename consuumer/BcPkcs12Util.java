import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCS8PrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS8EncryptedPrivateKeyInfoBuilder;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public final class BcPkcs12Util {

    private BcPkcs12Util() {}

    public static void generatePkcs12(String certPem, String keyPem, String alias, String password, String outputFilePath) throws Exception {
        // Parse certificates
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<Certificate> certs = new ArrayList<>();

        try (Reader reader = new StringReader(certPem);
             PEMParser pemParser = new PEMParser(reader)) {

            Object obj;
            while ((obj = pemParser.readObject()) != null) {
                if (obj instanceof org.bouncycastle.cert.X509CertificateHolder holder) {
                    certs.add(cf.generateCertificate(new ByteArrayInputStream(holder.getEncoded())));
                }
            }
        }

        if (certs.isEmpty()) {
            throw new IllegalArgumentException("No certificates found in certPem");
        }

        // Parse private key (PKCS#8)
        PrivateKey privateKey;
        try (Reader reader = new StringReader(keyPem);
             PEMParser pemParser = new PEMParser(reader)) {

            Object obj = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (obj instanceof org.bouncycastle.openssl.PEMKeyPair keyPair) {
                privateKey = converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            } else if (obj instanceof PKCS8EncryptedPrivateKeyInfo encKey) {
                // For encrypted PKCS8 keys, provide password if needed
                privateKey = converter.getPrivateKey(encKey.decryptPrivateKeyInfo(password.toCharArray()));
            } else if (obj instanceof PKCS8PrivateKeyInfo keyInfo) {
                privateKey = converter.getPrivateKey(keyInfo);
            } else {
                throw new IllegalArgumentException("Unsupported private key format");
            }
        }

        // Build PKCS12 keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, privateKey, password.toCharArray(), certs.toArray(new Certificate[0]));

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            ks.store(fos, password.toCharArray());
        }
    }
}
