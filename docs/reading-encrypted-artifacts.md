# Reading encrypted artifacts

Artifacts may be stored encrypted in the process archive. Such objects carry an `is_encrypted=true` entry
in their S3 user metadata. To read them, the `ProcessArchiveReader` must be created with a
`DecryptingStorageObjectRepository`, which decrypts the payload client-side via jEAP Crypto before Avro
deserialization.

## Prerequisites

- A `software.amazon.awssdk.services.s3.S3Client` for the archive bucket.
- A `ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService` from jEAP Crypto. See the
  [jeap-crypto](https://github.com/jeap-admin-ch/jeap-crypto) documentation on how to configure and
  inject a `KeyReferenceCryptoService` instance. The library declares `jeap-crypto-core` as a `provided`
  dependency, so the consuming service must bring jEAP Crypto on its classpath.

## Wiring the decrypting reader

The auto-configured `ProcessArchiveReader` bean uses a plain `S3StorageObjectRepository` and does **not**
decrypt. To read encrypted artifacts, construct your own reader with a `DecryptingStorageObjectRepository`:

```java
KeyReferenceCryptoService cryptoService = ...; // injected from jEAP Crypto
ProcessArchiveReader reader = new ProcessArchiveReader(
        new DecryptingStorageObjectRepository(s3Client, cryptoService));

MyArchiveType myObject = reader.readArtifact(MyArchiveType.class, bucket, key);
```

## How decryption is applied

`DecryptingStorageObjectRepository` extends `S3StorageObjectRepository`. After fetching the object it
inspects the S3 metadata: when `is_encrypted` is `true` it replaces the payload with
`cryptoService.decrypt(data)`; otherwise it returns the object unchanged. This means the same decrypting
reader transparently handles both encrypted and unencrypted objects in a bucket. The writer schema object
is read as-is and is not decrypted.

## Related

- [Getting started](getting-started.md)
- [How it works](how-it-works.md)
- [Configuration reference](configuration.md)
- [jeap-process-archive-reader](../README.md)
