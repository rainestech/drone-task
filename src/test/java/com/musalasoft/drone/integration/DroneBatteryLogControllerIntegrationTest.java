package com.musalasoft.drone.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.drone.MedicationController;
import com.musalasoft.drone.entity.DroneBatteryEventLog;
import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.repository.DroneBatteryRepository;
import com.musalasoft.drone.services.BatteryMonitorService;
import com.musalasoft.drone.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class DroneBatteryLogControllerIntegrationTest {
    private final MediaType jsonMediaType = MediaType.APPLICATION_JSON;
    private final String url = "/api/v1/drones/battery/log";
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MedicationController controller;

    @Autowired
    DroneBatteryRepository batteryRepository;

    @Autowired
    PopulateSampleData sampleData;

    @SpyBean
    BatteryMonitorService batteryMonitorService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
    }

    @Test
    @Order(1)
    void whenGettingLogsOfDroneBatteries_thenReturnCurrentLogInJson() throws Exception {
        await()
                // Duration set to 6 for test purposes so that the test won't take long to complete
                // the value of the fixedDelay on the class should also be modified to reflect
                // this value for test purposes
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> verify(batteryMonitorService,
                        atLeast(5)).droneBatteryMonitorStub());

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));
    }

    @Test
    @Order(2)
    void whenGettingLogsOfSpecificDroneBattery_thenReturnLogsOfSpecifiedDroneBatteryOnly() throws Exception {
        // used here to actively delay execution
        await()
                // Duration set to 6 for test purposes so that the test won't take long to complete
                // the value of the fixedDelay on the class should also be modified to reflect
                // this value for test purposes
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> verify(batteryMonitorService,
                        atLeast(5)).droneBatteryMonitorStub());

        List<DroneBatteryEventLog> batteryLog = batteryRepository.findAll();
        if (batteryLog.isEmpty()) {
            whenGettingLogsOfSpecificDroneBattery_thenReturnLogsOfSpecifiedDroneBatteryOnly();
            return;
        }

        Drones drone = batteryLog.get(0).getDrone();
        Optional<DroneBatteryEventLog> drone2 = batteryLog.stream()
                .filter(b -> !b.getDrone().equals(drone)).findFirst();

        if (drone2.isEmpty()) {
            whenGettingLogsOfSpecificDroneBattery_thenReturnLogsOfSpecifiedDroneBatteryOnly();
            return;
        }

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url + "/" + drone.getSerialNumber())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains(drone.getSerialNumber()));
        assertFalse(result.getResponse().getContentAsString().contains(drone2.get().getDrone().getSerialNumber()));
    }
}
