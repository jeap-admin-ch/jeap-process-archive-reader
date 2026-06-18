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

## Related

- [Getting started](getting-started.md)
- [How it works](how-it-works.md)
- [Reading encrypted artifacts](reading-encrypted-artifacts.md)
- [jeap-process-archive-reader](../README.md)
