# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the library in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

`jeap-process-archive-reader` is a single-module Maven library that reads and deserializes artifacts
from the jEAP Process Archive on S3-compatible object storage. It fetches a binary Avro object plus its
stored writer schema, resolves that schema against the reader schema of an Avro-generated class, and
returns the typed object. Spring Boot auto-configuration exposes a `ProcessArchiveReader` bean. The
library optionally decrypts artifacts client-side using jEAP Crypto.

## Repository layout

```
pom.xml                                                  # Single-module build (parent: jeap-spring-boot-parent)
src/main/java/ch/admin/bit/jeap/processarchive/reader/
  ProcessArchiveReader.java                              # Public API: readArtifact(...) overloads
  ProcessArchiveReaderException.java                     # RuntimeException with static factory methods
  ProcessArchiveReaderAutoConfiguration.java             # Registers the ProcessArchiveReader bean (@ConditionalOnBean S3Client)
  objectstorage/
    S3StorageObjectRepository.java                       # S3 GetObject / HeadObject access
    DecryptingStorageObjectRepository.java               # Subclass that decrypts via KeyReferenceCryptoService
    StorageObject.java                                   # record(key, data, metadata)
    S3ObjectStorageConfiguration.java                    # Builds an S3Client from connection properties (if none present)
    S3ObjectStorageConnectionProperties.java             # @ConfigurationProperties("jeap.process-archive.reader.connection")
src/main/resources/META-INF/spring/...AutoConfiguration.imports
src/test/...                                             # ProcessArchiveReaderIT, ProcessArchiveReaderTest, Avro fixtures (v1/v2/v3)
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

## Build & test

```bash
./mvnw verify          # full build incl. tests
./mvnw test
```

- Parent: `ch.admin.bit.jeap:jeap-spring-boot-parent` (Spring Boot 4 aligned).
- Tests mock the AWS `S3Client` (`@MockitoBean`); Avro test schemas live under
  `src/test/resources/avro/v1|v2|v3` and exercise schema evolution / resolution.
- `jeap-crypto-core` is a `provided` dependency — decryption support compiles against it but the
  consuming service must bring jEAP Crypto on the classpath to use `DecryptingStorageObjectRepository`.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.processarchive.reader`.
- Configuration properties use the prefix `jeap.process-archive.reader.connection.*`.
- Auto-configuration is registered via
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- The reader relies on object metadata written by the producer side (`jeap-process-archive-service`):
  `schema-file-key` points to the writer schema object, `is_encrypted` flags encrypted payloads.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per
file) and the documentation index in the README.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POM.
- Always keep the `-SNAPSHOT` postfix in the POM; CI removes it when releasing. Do not use the SNAPSHOT
  postfix elsewhere (CHANGELOG, `publiccode.yml`).
- Keep changelog entries concise; follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional
  commits (for example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog and the version/date in `publiccode.yml`.
