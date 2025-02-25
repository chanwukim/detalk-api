package net.detalk.api.infrastructure.bootstrap;


import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.role.domain.Role;
import net.detalk.api.plan.service.PricingPlanCache;
import net.detalk.api.role.repository.RoleRepository;
import net.detalk.api.support.EnvironmentHolder;
import net.detalk.api.support.security.SecurityRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetalkInitializer implements ApplicationRunner {

    private final PricingPlanCache pricingPlanCache;

    private final AlarmSender alarmSender;

    private final RoleRepository roleRepository;

    private final EnvironmentHolder env;

    @Transactional
    @Override
    public void run(ApplicationArguments args){
        // 가격 정책 캐싱
        pricingPlanCache.loadPricingPlans();

        // 트랜잭션과 무관한 알람 시스템 초기화
        initializeDiscord();

        // 초기 권한
        initMemberRoles();

        if ("prod".equals(env.getActiveProfile())) {
            log.info("운영 서버 톰캣 실행 완료");
            alarmSender.sendMessage("운영 서버 톰캣 실행 완료");
        }
    }

    private void initializeDiscord() {
        try {
            alarmSender.initialize();
        }catch (RuntimeException e) {
            log.error("alarmSender 봇 초기화 실패: {}", e.getMessage(), e);
        }
    }

    private void initMemberRoles() {

        // SecurityRole 클래스에 정의되어있는 모든 권한 목록을 조회한다
        List<String> roleCodes = Arrays.stream(SecurityRole.values())
            .map(SecurityRole::getName)
            .toList();

        // DB에 존재하는 모든 권한을 조회한다
        List<Role> existingRoles = roleRepository.findByCodes(roleCodes);

        // DB에 존재하는 권한 목록의 Code 추출
        Set<String> existingCodes = existingRoles.stream()
            .map(Role::getCode)
            .collect(Collectors.toSet());

        // DB에 존재하지 않을 경우, 새 권한을 저장한다.
        for (SecurityRole securityRole : SecurityRole.values()) {
            if (!existingCodes.contains(securityRole.getName())) {
                roleRepository.save(
                    new Role(securityRole.getName(), securityRole.getDescription()));
                log.info("새 권한 생성 : {}", securityRole.getName());
            }
        }

    }

}
