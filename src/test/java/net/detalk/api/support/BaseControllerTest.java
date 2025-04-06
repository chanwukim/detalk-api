package net.detalk.api.support;

import static org.mockito.Mockito.mock;

import java.util.List;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.support.config.AppProperties;
import net.detalk.api.support.config.WebConfig;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 모든 컨트롤러 슬라이스 테스트를 위한 기본 클래스.
 * 공통 Mock 빈, 설정 Import, MockMvc 주입, 인증 헬퍼 메서드를 제공합니다.
 * WebConfig 및 공통 TestConfig 임포트
 */
@Import({WebConfig.class, BaseControllerTest.CommonWebLayerTestConfig.class})
public abstract class BaseControllerTest {

    // IDE 잘못된 컴파일 경고 무시
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected MockMvc mockMvc; // 하위 클래스에서 사용 가능하도록 protected

    // 공통으로 필요한 Mock 빈 및 설정 빈 정의
    @TestConfiguration
    static class CommonWebLayerTestConfig {

        // WebConfig 의존성 빈
        @Bean
        public AppProperties appProperties() {
            return new AppProperties();
        }

        // WebExceptionHandler 의존성 Mock 빈
        @Bean
        public AlarmSender alarmSender() {
            return mock(AlarmSender.class);
        }
    }

    // 테스트용 Authentication 객체 생성 헬퍼 메서드
    protected Authentication createTestAuthentication(Long memberId, SecurityRole role) {
        SecurityUser testUser = new SecurityUser(
            memberId,
            List.of(new SimpleGrantedAuthority(role.getName()))
        );
        return new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    }
}
