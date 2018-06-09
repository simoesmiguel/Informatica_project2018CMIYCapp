package com.example.someoneelse.library;

import android.util.Base64;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.interfaces.DHPublicKey;

public final class KeyExchange {

    private KeyPair myKeyPair = null;

    private KeyAgreement myKeyAgree = null;

    private PublicKey otherPublicKey = null;

    private byte[] sharedSecret = null;

    private SecretKey secretKey = null;

    private static final String DH_RFC3526_G14 = (
            "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
                    "29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
                    "EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
                    "E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
                    "EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
                    "C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
                    "83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
                    "670C354E 4ABC9804 F1746C08 CA237327 FFFFFFFF FFFFFFFF")
            .replaceAll("\\s", "");

    private BigInteger prime = new BigInteger(DH_RFC3526_G14, 16);
    private BigInteger generator = BigInteger.valueOf(2L);

    private String name;

    public KeyExchange(String name) {
        this.name = name;
    }

    public void generateDhKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        System.out.println(name + " is generating key pair...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        DHParameterSpec dhGrp14KeySpec = new DHParameterSpec(prime, generator);
        keyGen.initialize(dhGrp14KeySpec);
        myKeyPair = keyGen.generateKeyPair();
    }

    public void init()
            throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println(name + " is initializing...");
        myKeyAgree = KeyAgreement.getInstance("DH");
        myKeyAgree.init(myKeyPair.getPrivate());
    }

    public String getEncodedPublicKey() {
        return Base64.encodeToString(myKeyPair.getPublic().getEncoded(),Base64.NO_WRAP);
    }

    public void generateDhPublicKeyFromOther(String encodedKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decode(encodedKey,Base64.NO_WRAP));
        otherPublicKey = keyFac.generatePublic(x509KeySpec);
    }

    public void generateDhKeyPairFromOther()
            throws NoSuchAlgorithmException,InvalidAlgorithmParameterException {
        System.out.println(name + " is generating key pair... (from received)");
        DHParameterSpec params = ((DHPublicKey) otherPublicKey).getParams();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        keyGen.initialize(params);
        myKeyPair = keyGen.generateKeyPair();
    }

    public void firstPhase()
            throws InvalidKeyException, IllegalStateException {
        System.out.println(name + " is executing phase...");
        System.out.println("Alice KeyAgreement: " + myKeyAgree);
        myKeyAgree.doPhase(otherPublicKey, true);
    }

    public void generateSecret() {
        sharedSecret = myKeyAgree.generateSecret();
    }

    public void generateSecretKey(byte[] sharedSecret) {
        secretKey =  new SecretKeySpec(sharedSecret, 0, 16, "AES");
    }

    public void setSharedSecretLength(int length) {
        sharedSecret = new byte[length];
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

}
