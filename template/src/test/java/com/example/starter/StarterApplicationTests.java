package com.example.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 애플리케이션 컨텍스트 로드 기본 테스트
 * - src/test/resources/application.yml 에 의해 항상 local(H2) 프로파일로 실행
 */
@SpringBootTest
class StarterApplicationTests {

    @Test
    void contextLoads() {
        // Spring 컨텍스트가 정상적으로 로드되는지 확인
    }
}
