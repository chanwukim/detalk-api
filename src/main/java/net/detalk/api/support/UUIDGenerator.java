package net.detalk.api.support;


import java.util.UUID;

public interface UUIDGenerator {

    UUID generateV4();

    UUID generateV7();

    UUID fromString(String name);
}
