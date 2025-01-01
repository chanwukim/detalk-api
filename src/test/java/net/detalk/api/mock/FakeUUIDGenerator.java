package net.detalk.api.mock;

import java.util.UUID;
import net.detalk.api.support.UUIDGenerator;

public class FakeUUIDGenerator implements UUIDGenerator {

    private final UUID uuid;

    /**
     *
     * @param UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
     */
    public FakeUUIDGenerator(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID generateV4() {
        return uuid;
    }

    @Override
    public UUID generateV7() {
        return uuid;
    }

    @Override
    public UUID fromString(String name) {
        return uuid;
    }

}
