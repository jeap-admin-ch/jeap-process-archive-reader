# Configuration reference

`ProcessArchiveReader` needs an AWS `software.amazon.awssdk.services.s3.S3Client` to reach the archive
bucket. There are two ways to provide it.

## Option A: reuse an existing `S3Client` bean

If your service already exposes an `S3Client` bean, the library reuses it. The auto-configuration creates
the `ProcessArchiveReader` bean only when an `S3Client` bean is present
(`@ConditionalOnBean(S3Client.class)`), and runs after the library's own S3 configuration
(`@AutoConfigureAfter(S3ObjectStorageConfiguration.class)`).

## Option B: let the library build an `S3Client`

When no `S3Client` bean exists, the library builds one from connection properties — but only if
`jeap.process-archive.reader.connection.access-url` is set (`@ConditionalOnProperty`) and no other
`S3Client` bean is defined (`@ConditionalOnMissingBean`). The built client uses path-style access and the
URL-connection HTTP client.

```yaml
jeap:
  process-archive:
    reader:
      connection:
        access-url: <access-url>
        access-key: <access-key>
        secret-key: <secret-key>
        region: eu-central-1
```

## Properties

All properties use the prefix `jeap.process-archive.reader.connection`.

| Property      | Default      | Description                                                                                                   |
|---------------|--------------|---------------------------------------------------------------------------------------------------------------|
| `access-url`  | —            | Endpoint of the S3-compatible object storage. Required to trigger the built-in `S3Client`. `http(s)://` is added if no scheme is given |
| `access-key`  | —            | Access key. When `access-key` and `secret-key` are both set, static credentials are used                      |
| `secret-key`  | —            | Secret key (paired with `access-key`)                                                                         |
| `region`      | `aws-global` | AWS region; parsed via `Region.of(...)`                                                                       |

When `access-key` or `secret-key` is missing, the client falls back to the AWS
`DefaultCredentialsProvider` (environment, profile, container/instance role, etc.). `access-key` and
`secret-key` are excluded from the properties' `toString()` so they are not logged.

## Avro class whitelist

Since Avro 1.12.2, Avro resolves a class from a schema only if that class is trusted. Reading an archived object
deserializes it, so a whitelist has to be installed before the first read — otherwise the read fails with a
`SecurityException`. **This library does not install one**, because the whitelist is global, static state in Avro that
is installed exactly once per JVM: installing it belongs to the application, not to a library it happens to use.

### Applications using jeap-messaging

Nothing to do. The `AvroClassSecurityAutoConfiguration` of jeap-messaging installs the whitelist before any other bean
is created, and therefore before an archived object can be read. Packages and classes to trust beyond the defaults are
configured there — that is the single place they belong:

```yaml
jeap:
  messaging:
    avro:
      trusted-packages: com.example.archive
      trusted-classes: com.example.CustomLogicalType
```

### Applications not using jeap-messaging

Install the whitelist yourself, before the first archived object is read — in the `main` method, in a
`BeanFactoryPostProcessor`, or in a `@BeforeAll` method of a test:

```java
// The jEAP default whitelist: the Avro generated types in ch.admin
AvroClassSecurity.installDefaultIfMissing();

// Or an explicit whitelist, if archived objects live outside ch.admin
AvroClassSecurity.install(List.of("com.example.archive"), List.of());
```

`AvroClassSecurity` comes with `ch.admin.bit.jeap:jeap-messaging-avro`, which such an application has to add itself —
this library only uses it in its own tests.

## Related

- [Getting started](getting-started.md)
- [How it works](how-it-works.md)
- [Reading encrypted artifacts](reading-encrypted-artifacts.md)
- [jeap-process-archive-reader](../README.md)
