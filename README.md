# jEAP Process Archive Reader - Library

## Getting started

This library can be used to retrieve an object from the process archive (S3) and convert it directly into the target
object.
The writer schema will be read from the archive and if the reader schema is compatible (the one from the generated
class), the object will be returned.
If the two schemas are not compatible, the library throws a `ProcessArchiveReaderException`.

## Usage

If the library is added to a service's dependencies, the `ProcessArchiveReader` bean will be automatically instantiated.

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-process-archive-reader</artifactId>
</dependency>
```

The client can be then autowired:

```java
@Autowired
private ProcessArchiveReader archiveReader;
```

The client can retrieve the current version of an object of type `MyArchiveType` (MyArchiveType is AvroGenerated):

```java
MyArchiveType myObject = archiveReader.readArtifact(MyArchiveType.class, bucket, key);
```

The client can also retrieve a specific version of an object:
```java
MyArchiveType myObject = archiveReader.readArtifact(MyArchiveType.class, bucket, key, version);
```

### Reading encrypted objects

To read objects encrypted with a specific key, create a `ProcessArchiveReader` instance with
a `DecryptingObjectStorageRepository` as shown in the following example:

```java
KeyReferenceCryptoService cryptoService = ...;
ProcessArchiveReader reader = new ProcessArchiveReader(
        new DecryptingObjectStorageRepository(s3Client, cryptoService));
```

## S3Client

The `ProcessArchiveReader` needs a `S3Client` (software.amazon.awssdk.services.s3.S3Client) to access the bucket.
If no `S3Client` is available in the context, a new `S3Client` can be instantiated using the following properties:

```yaml
jeap:
  processarchive:
    reader:
      connection:
        access-url: <access-url>
        access-key: <access-key>
        secret-key: <secret-key>
```

## Changes

This library needs to be versioned using [Semantic Versioning](http://semver.org/) and all changes need to be documented
at [CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/)


## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
