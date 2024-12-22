package net.detalk.api.support;

import com.fasterxml.uuid.Generators;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDGenerator {
    public UUID generateV4() {
        return Generators.randomBasedGenerator().generate();
    }

    public UUID generateV7() {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
