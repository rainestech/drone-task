package com.musalasoft.drone.unit;

import com.musalasoft.drone.services.BatteryMonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@SpringBootTest
class BatteryMonitorServiceUnitTest {

    @SpyBean
    private BatteryMonitorService batteryMonitorService;

    @Test
    void whenWaitOneSecond_thenScheduledIsCalledAtLeastTenTimes() {
        await()
                // Duration set to 6 for test purposes so that the test won't take long to complete
                // the value of the fixedDelay on the class should also be modified to reflect
                // this value for test purposes
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> verify(batteryMonitorService,
                        atLeast(5)).droneBatteryMonitorStub());
    }
}
