package ch.admin.bit.jeap.processarchive.reader.objectstorage;

import java.util.Map;

public record StorageObject(String key, byte[] data, Map<String, String> metadata) {
}
