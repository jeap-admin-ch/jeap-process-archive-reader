# Getting started

This page shows how to add `jeap-process-archive-reader` to a Spring Boot service and read an archived
artifact back into a typed object. For the mechanics behind the read see [How it works](how-it-works.md);
for the S3 connection options see the [Configuration reference](configuration.md).

## 1. Add the dependency

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-process-archive-reader</artifactId>
</dependency>
```

The version is managed by the jEAP Spring Boot parent. Adding the dependency activates the library's
auto-configuration.

## 2. Provide the Avro reader type

The library deserializes an artifact into an Avro-generated Java class (the *reader* type). Add the
Maven dependency that contains the generated archive-type bindings, or generate them from the archive
schema with the Avro Maven plugin, so the target class (for example `MyArchiveType`) is on the classpath.

## 3. Provide an S3Client

`ProcessArchiveReader` needs a `software.amazon.awssdk.services.s3.S3Client` to reach the archive bucket.
Either expose an `S3Client` bean yourself, or let the library build one from connection properties — see
the [Configuration reference](configuration.md). The `ProcessArchiveReader` bean is only created when an
`S3Client` bean is present (`@ConditionalOnBean(S3Client.class)`).

## 4. Inject the reader

```java
@Autowired
private ProcessArchiveReader archiveReader;
```

## 5. Read an artifact

Read the current version of an object of type `MyArchiveType` (an Avro-generated class) from a bucket
and key:

```java
MyArchiveType myObject = archiveReader.readArtifact(MyArchiveType.class, bucket, key);
```

Read a specific S3 object version instead:

```java
MyArchiveType myObject = archiveReader.readArtifact(MyArchiveType.class, bucket, key, version);
```

A leading slash in the key is stripped automatically. If the object cannot be read or its writer schema
is incompatible with `MyArchiveType`, a `ProcessArchiveReaderException` is thrown.

## Related

- [How it works](how-it-works.md)
- [Configuration reference](configuration.md)
- [Reading encrypted artifacts](reading-encrypted-artifacts.md)
- [jeap-process-archive-reader](../README.md)
