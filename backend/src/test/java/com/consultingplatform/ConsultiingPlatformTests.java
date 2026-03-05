package com.consultingplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ConsultiingPlatform.class)
@ActiveProfiles("test")
class ConsultiingPlatformTests {

    @Test
    void contextLoads() {
    }
}
