package com.telefonica.jwk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Entrypoint {
  private static final String DEFAULT_KEY_SIZE = "2048";
  private static final Options OPTIONS = new Options();
  private static final JsonParser JSON = new JsonParser();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static String size = null;
  private static KeyUse keyUse = null;
  private static Algorithm algorithm = JWEAlgorithm.RSA_OAEP_256;
  private static boolean keySet = true;
  private static String outFile = null;

  public static void main(String[] args) {

    // init Cryptographic provider as plain java does not support all the algorithms (particularlly RSA and AES galois)
    Security.addProvider(new BouncyCastleProvider());


    OPTIONS.addOption("s", true, "Key Size in bits, required for RSA and oct key types. Must be an integer divisible by 8");
    OPTIONS.addOption("u", true, "Usage, one of: enc, sig (optional)");
    OPTIONS.addOption("S", false, "Wrap the generated key in a KeySet");
    OPTIONS.addOption("o", true, "Write output to file");


    parseCommandLine(args);

    try {

      size = Optional.ofNullable(size)
        .orElse(DEFAULT_KEY_SIZE);

      Integer keySize = Integer.decode(size);
      if (keySize % 8 != 0) {
        printUsageAndExit("Key size (in bits) must be divisible by 8, got " + keySize);
      }


      JWK jwk = RSAKeyMaker.make(keySize, keyUse, algorithm);

      if (outFile == null) {
        printKey(keySet, jwk);
      } else {
        writeKeyToFile(keySet, outFile, jwk);
      }
    } catch (IOException e) {
      printUsageAndExit("Could not read write to File the KeySet: " + e.getMessage());
    }

  }

  private static void parseCommandLine(String[] args) {
    CommandLineParser parser = new PosixParser();
    try {
      CommandLine cmd = parser.parse(OPTIONS, args);

      size = cmd.getOptionValue("s");
      String use = cmd.getOptionValue("u");
      keySet = cmd.hasOption("S");

      keyUse = null;
      if (use != null) {
        if (use.equals("sig")) {
          keyUse = KeyUse.SIGNATURE;
          algorithm = JWSAlgorithm.RS256;
        } else if (use.equals("enc")) {
          keyUse = KeyUse.ENCRYPTION;
        } else {
          printUsageAndExit("Invalid key usage, must be 'sig' or 'enc', got " + use);
        }
      }

      outFile = cmd.getOptionValue("o");

    } catch (NumberFormatException e) {
      printUsageAndExit("Invalid key size: " + e.getMessage());
    } catch (ParseException e) {
      printUsageAndExit("Failed to parse arguments: " + e.getMessage());
    }

  }

  private static void writeKeyToFile(boolean keySet, String outFile, JWK jwk) throws IOException {
    JsonElement json;
    File output = new File(outFile);
    if (keySet) {
      JWKSet jwkSet = new JWKSet(jwk);
      json = JSON.parse(jwkSet.toJSONObject(false).toJSONString());
    } else {
      json = JSON.parse(jwk.toJSONString());
    }
    Writer os = null;
    try {
      os = new BufferedWriter(new FileWriter(output));
      os.write(GSON.toJson(json));
      String keySetMsg = keySet ? "keyset" : "keys";
      System.out.println("Writed JWK " + keySetMsg + " to " + outFile);
    } finally {
      os.close();
    }
  }


  private static void printKey(boolean keySet, JWK jwk) {
    if (keySet) {
      JWKSet jwkSet = new JWKSet(jwk);
      JsonElement json = JSON.parse(jwkSet.toJSONObject(false).toJSONString());
      System.out.println(GSON.toJson(json));
    } else {
      JsonElement json = JSON.parse(jwk.toJSONString());
      System.out.println(GSON.toJson(json));
    }
  }

  // print out a usage message and quit
  private static void printUsageAndExit(String message) {
    if (message != null) {
      System.err.println(message);
    }

    List<String> optionOrder = Arrays.asList("s", "u");

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar json-web-key-generator.jar -t <keyType> [options]", OPTIONS);

    // kill the program
    System.exit(1);
  }
}
