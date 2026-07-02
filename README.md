# jEAP Process Archive Reader

`jeap-process-archive-reader` is a small Spring Boot library for reading and deserializing artifacts
that a service archived in the jEAP Process Archive on S3-compatible object storage. Given a bucket and
object key it fetches the binary Avro object, reads the writer schema that was stored alongside it, and
deserializes it directly into the generated reader type. If the writer and reader schemas are not
compatible the read fails with a `ProcessArchiveReaderException`. It provides:

* A `ProcessArchiveReader` bean that returns archived objects as strongly-typed, Avro-generated Java objects
* Schema-on-read: the writer schema is fetched from the archive and resolved against the reader schema
* Retrieval of the current object version or a specific S3 object version
* Client-side decryption of encrypted artifacts via jEAP Crypto (`DecryptingStorageObjectRepository`)
* Auto-configuration that reuses an existing `S3Client` bean or builds one from connection properties

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                                     | File                                                                       |
|-----------------------------------------------------------|----------------------------------------------------------------------------|
| Getting started (add the dependency, read an artifact)    | [docs/getting-started.md](docs/getting-started.md)                         |
| How it works (schema-on-read, S3 layout, versions)        | [docs/how-it-works.md](docs/how-it-works.md)                               |
| Configuration reference (`jeap.process-archive.reader.*`) | [docs/configuration.md](docs/configuration.md)                             |
| Reading encrypted artifacts                               | [docs/reading-encrypted-artifacts.md](docs/reading-encrypted-artifacts.md) |

## Modules

This is a single-module library. The group id is `ch.admin.bit.jeap`; the version is managed by the
jEAP Spring Boot parent.

| Artifact                      | Purpose                                                                                     |
|-------------------------------|---------------------------------------------------------------------------------------------|
| `jeap-process-archive-reader` | `ProcessArchiveReader`, the S3 storage repositories and the Spring Boot auto-configuration |

## Changes

This library needs to be versioned using [Semantic Versioning](http://semver.org/) and all changes need to be documented
at [CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/)

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
