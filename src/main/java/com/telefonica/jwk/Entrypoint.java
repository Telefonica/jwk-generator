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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Entrypoint {
  private static final String DEFAULT_KEY_SIZE = "2048";
  private static final Options OPTIONS = new Options();
  private static final JsonElement JSON = new JsonParser();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static void main(String[] args) {

    Security.addProvider(new BouncyCastleProvider());


    options.addOption("s", true, "Key Size in bits, required for RSA and oct key types. Must be an integer divisible by 8");
    options.addOption("u", true, "Usage, one of: enc, sig (optional)");
    options.addOption("S", false, "Wrap the generated key in a KeySet");

    CommandLineParser parser = new PosixParser();
    try {
      CommandLine cmd = parser.parse(options, args);

      String size = cmd.getOptionValue("s");
      String use = cmd.getOptionValue("u");
      boolean keySet = cmd.hasOption("S");
      Algorithm algorithm = JWEAlgorithm.RSA_OAEP_256;

      KeyUse keyUse = null;
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


      // surrounding try/catch catches numberformatexception from this
      if (Objects.isNull(size)) {
        size = DEFAULT_KEY_SIZE;
      }

      Integer keySize = Integer.decode(size);
      if (keySize % 8 != 0) {
        printUsageAndExit("Key size (in bits) must be divisible by 8, got " + keySize);
      }

      JWK jwk = RSAKeyMaker.make(keySize, keyUse, algorithm);

      // round trip it through GSON to get a prettyprinter
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      printKey(keySet, jwk, gson);
    } catch (NumberFormatException e) {
      printUsageAndExit("Invalid key size: " + e.getMessage());
    } catch (ParseException e) {
      printUsageAndExit("Failed to parse arguments: " + e.getMessage());
    }
  }


  private static void printKey(boolean keySet, JWK jwk, Gson gson) {
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
    formatter.printHelp("java -jar json-web-key-generator.jar -t <keyType> [options]", options);

    // kill the program
    System.exit(1);
  }
}
