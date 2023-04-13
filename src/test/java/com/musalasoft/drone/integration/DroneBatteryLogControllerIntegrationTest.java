package com.musalasoft.drone.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.drone.MedicationController;
import com.musalasoft.drone.entity.DroneBatteryEventLog;
import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.repository.DroneBatteryRepository;
import com.musalasoft.drone.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
    }

    @Test
    @Order(1)
    void whenGettingLogsOfDroneBatteries_thenReturnCurrentLogInJson() throws Exception {
        // using awaitility to delay execution for the scheduler to have some data before queries
        await().atLeast(10, TimeUnit.SECONDS);

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));
    }

    @Test
    @Order(2)
    void whenGettingLogsOfSpecificDroneBattery_thenReturnLogsOfSpecifiedDroneBatteryOnly() throws Exception {
        // using awaitility to delay execution for the scheduler to have some data before queries
        await().atLeast(10, TimeUnit.SECONDS);

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
