package com.telefonica.jwk;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSAKeyMaker {

  /**
   * Generate a kid using the sha-1 digest of public exponent
   *
   * @param publicModulus
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static String makeKid(BigInteger publicModulus) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    md.update(publicModulus.toByteArray());
    BigInteger kid = new BigInteger(1, md.digest());
    return kid.toString(16); //hex string
  }


  /**
   * Generate a new RSAKey with the kid specified.
   *
   * @param keySize
   * @param keyUse
   * @param keyAlg
   * @return
   */
  public static RSAKey make(Integer keySize, KeyUse keyUse, Algorithm keyAlg) {

    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(keySize);
      KeyPair keyPair = generator.generateKeyPair();

      RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
      RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

      String kid = makeKid(pub.getModulus());

      RSAKey rsaKey = new RSAKey.Builder(pub)
        .privateKey(priv)
        .keyUse(keyUse)
        .algorithm(keyAlg)
        .keyID(kid)
        .build();

      return rsaKey;
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }
}
