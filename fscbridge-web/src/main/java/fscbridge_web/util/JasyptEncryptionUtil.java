package fscbridge_web.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptEncryptionUtil {

    public static void main(String[] args) {

       String masterPassword = "fscbridge-secret-key-2025";

        String orgUrl = "";
        String clientId = "";
        String clientSecret = "";
        String geminiApiKey = "";
        StandardPBEStringEncryptor encryptor =
                new StandardPBEStringEncryptor();
        encryptor.setPassword(masterPassword);
        encryptor.setAlgorithm("PBEWithMD5AndDES");

        String encryptedOrgUrl = encryptor.encrypt(orgUrl);
        String encryptedClientId = encryptor.encrypt(clientId);
        String encryptedClientSecret = encryptor.encrypt(clientSecret);
        String encryptedGeminiKey = encryptor.encrypt(geminiApiKey);

        System.out.println("\n=== Copy these into application.yml ===");
        System.out.println("orgUrl:       ENC(" + encryptedOrgUrl + ")");
        System.out.println("clientId:     ENC(" + encryptedClientId + ")");
        System.out.println("clientSecret: ENC(" + encryptedClientSecret + ")");
        System.out.println("geminiApiKey: ENC(" + encryptedGeminiKey + ")");
        System.out.println("=======================================\n");
        System.out.println("Start app with:");
        System.out.println("-Djasypt.encryptor.password=fscbridge-secret-key-2025");
    }
}