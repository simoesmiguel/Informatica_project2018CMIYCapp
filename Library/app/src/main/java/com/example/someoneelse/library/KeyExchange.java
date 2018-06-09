package cmiyc;

import android.util.Base64;
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
    
    private String name = "";
    
    public KeyExchange(String name) {
    	this.name = name;
    }
    
    public void generateDhKeyPair() 
    		throws NoSuchAlgorithmException {
    	System.out.println(name + " is generating key pair...");
    	KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        System.out.println("generateGhKeyPair 1");
    	keyGen.initialize(1024);
        System.out.println("generateGhKeyPair 2");
            myKeyPair = keyGen.generateKeyPair();
        System.out.println("generateGhKeyPair 3");
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
