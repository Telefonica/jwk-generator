# jwk-generator
A commandline Java-based generator for JSON Web Keys (JWK). By now only RSA keys are supported.

# Development

To compile, run `mvn package`. This will generate a `jwk-generator-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `/target` directory.

To generate a key, run `java -jar target/jwk-generator-0.5-SNAPSHOT-jar-with-dependencies.jar`. 

You can also define the following arguments:

```
 -s <arg>   Key Size in bits. Must be an integer divisible by 8. By default 2048.
 -u <arg>   Usage, one of: enc, sig (optional). By default enc.
 -S         Wrap the generated key in a KeySet
 -o <arg>   Write output to file
```

# Delivery

## Building locally

Run the command 
```
./delivery/scripts/docker-package.sh --image=jwk-generator:1.0
```

You can execute the following command to get an RSA keys in JWK format:

```
docker run jwk-generator:1.0
```

Write to a file in the docker file system (useful for shared volumes):
```
docker run jwk-generator:1.0 -o /usr/volumes/keys.txt
```

## Building and publishing

You should define the environment variables with the repository:
```
export REGISTRY_SERVER=myrepo.example.com
export REGISTRY_USER=xxx
export REGISTRY_PASSWORD=xxxx

Run the command 
```
./delivery/pipelines/publish-artifact.sh --version=1.0
```

This will do the building of the docker imagen and pull to the remote registry with the tag (baikal/jwk-generator:${VERSION})
