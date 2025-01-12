package net.detalk.api.support;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SystemEnvironmentHolder implements EnvironmentHolder{

    private final Environment environment;

    @Override
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }
}
