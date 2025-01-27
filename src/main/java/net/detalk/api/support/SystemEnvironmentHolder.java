package net.detalk.api.support;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SystemEnvironmentHolder implements EnvironmentHolder{

    private static final String DEFAULT_PROFILE = "dev";
    private final Environment env;

    @Override
    public String[] getActiveProfiles() {
        return env.getActiveProfiles();
    }

    @Override
    public String getActiveProfile() {
        String[] activeProfiles = env.getActiveProfiles();

        if (activeProfiles.length == 0) {
            return Optional.of(env.getDefaultProfiles())
                .filter(defaults -> defaults.length >0)
                .map(defaults -> defaults[0])
                .orElse(DEFAULT_PROFILE);
        }

        List<String> priorityOrder = Arrays.asList("dev", "test", "prod");

        return Arrays.stream(activeProfiles)
            .filter(priorityOrder::contains)
            .findFirst()
            .orElse(activeProfiles[0]);
    }
}
