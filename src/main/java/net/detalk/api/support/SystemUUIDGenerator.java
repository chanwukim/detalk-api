package net.detalk.api.support;

import com.fasterxml.uuid.Generators;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SystemUUIDGenerator implements UUIDGenerator{

    @Override
    public UUID generateV4() {
        return Generators.randomBasedGenerator().generate();
    }

    @Override
    public UUID generateV7() {
        return Generators.timeBasedEpochGenerator().generate();
    }

    @Override
    public UUID fromString(String name) {
        return UUID.fromString(name);
    }

}
