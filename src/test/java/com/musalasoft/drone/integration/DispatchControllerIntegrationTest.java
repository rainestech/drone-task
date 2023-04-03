package com.musalasoft.drone.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.drone.DispatchController;
import com.musalasoft.drone.dto.ChangeStateRequest;
import com.musalasoft.drone.dto.DroneStateOptions;
import com.musalasoft.drone.dto.LoadDifferentMedicationRequest;
import com.musalasoft.drone.dto.LoadMedicationRequest;
import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.entity.Medications;
import com.musalasoft.drone.repository.DroneRepository;
import com.musalasoft.drone.repository.MedicationRepository;
import com.musalasoft.drone.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class DispatchControllerIntegrationTest {

    private final MediaType jsonMediaType = MediaType.APPLICATION_JSON;
    private final String url = "/api/v1/dispatch";
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    DispatchController controller;

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    MedicationRepository medicationRepository;

    @Autowired
    PopulateSampleData sampleData;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
        sampleData.populateMedication();
    }

    @Test
    void whenRegisterNewDrone_thenReturnDroneDataWithJsonResponse() throws Exception {
        String data = """
                {
                        "serialNumber": "TEST_DATA_DISPATCH",
                        "weight": 0.0,
                        "batteryCapacity": 85,
                        "model": "CRUISERWEIGHT",
                        "state": "RETURNING",
                        "loadedMedications": []
                    }""";

        mockMvc.perform(MockMvcRequestBuilders.post(url + "/register")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(data));
    }

    @Test
    void whenLoadDroneWithMedication_thenReturnDroneWithLoadedMedication() throws Exception {
        Drones drone = droneRepository.findAll().get(0);
        Medications med = medicationRepository.findAll().get(0);

        // prepare drone for loading
        drone.setState(DroneStateOptions.LOADING);
        drone.setBatteryCapacity(100);
        droneRepository.save(drone);

        MvcResult res = mockMvc.perform(MockMvcRequestBuilders
                .get(url + "/load/" + drone.getSerialNumber() + "/" + med.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        assertTrue(res.getResponse().getContentAsString().contains(drone.getSerialNumber()));
        assertTrue(res.getResponse().getContentAsString().contains(med.getCode()));
    }

    @Test
    void whenLoadDroneWithMedicationsGreaterThan500Grams_thenReturn4XXClientErrors() throws Exception {
        Drones drone = droneRepository.findAll().get(0);
        Medications med = medicationRepository.findAll().get(0);

        // prepare drone for loading
        drone.setState(DroneStateOptions.LOADING);
        drone.setBatteryCapacity(100);
        droneRepository.save(drone);

        LoadMedicationRequest req = new LoadMedicationRequest();
        req.setQuantity(600);
        req.setDrone(drone);
        req.setMedication(med);


        String jsonData = mapper.writeValueAsString(req);

        mockMvc.perform(MockMvcRequestBuilders.post(url + "/load")
                .content(jsonData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json

        // Overloading loading with get method
        mockMvc.perform(MockMvcRequestBuilders
                .get(url + "/load/" + drone.getSerialNumber() + "/" + med.getCode() + "/40")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json

        // Overloading with multiple medications
        LoadDifferentMedicationRequest diffReq = new LoadDifferentMedicationRequest();
        diffReq.setMedications(medicationRepository.findAll());
        diffReq.setDrone(drone);

        String diffData = mapper.writeValueAsString(diffReq);
        mockMvc.perform(MockMvcRequestBuilders.post(url + "/load/diff")
                .content(diffData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json
    }

    @Test
    void whenLoadDroneNotInLoadingOrIdleState_thenReturn4XXClientErrors() throws Exception {
        Drones drone = droneRepository.findAll().get(0);
        Medications med = medicationRepository.findAll().get(0);

        // prepare drone for loading
        drone.setState(DroneStateOptions.LOADED);
        droneRepository.save(drone);

        LoadMedicationRequest req = new LoadMedicationRequest();
        req.setQuantity(1);
        req.setDrone(drone);
        req.setMedication(med);


        String jsonData = mapper.writeValueAsString(req);

        mockMvc.perform(MockMvcRequestBuilders.post(url + "/load")
                .content(jsonData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json
    }

    @Test
    void whenGettingLoadedItemsOnDrone_thenReturnDroneLoadedItems() throws Exception {
        Drones drone = droneRepository.findAll().get(0);
        Medications med = medicationRepository.findAll().get(0);

        // preload drone
        drone.setState(DroneStateOptions.LOADING);
        drone.setBatteryCapacity(100);

        List<Medications> load = new ArrayList<>();
        load.add(med);
        drone.setLoadedMedications(load);

        droneRepository.save(drone);

        String loadedDroneJson = mapper.writeValueAsString(drone.getLoadedMedications());

        // Using controller get method
        mockMvc.perform(MockMvcRequestBuilders
                .get(url + "/items/" + drone.getSerialNumber())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(loadedDroneJson));

        // Using controller post method
        String jsonRequestData = mapper.writeValueAsString(drone);
        mockMvc.perform(MockMvcRequestBuilders.post(url + "/items")
                .content(jsonRequestData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(loadedDroneJson));
    }

    @Test
    void whenChangeStateOfLowBatteryDroneToLoading_thenThrow4XXClientError() throws Exception {
        Drones drone = droneRepository.findAll().get(0);

        // preload drone
        drone.setState(DroneStateOptions.RETURNING);
        drone.setBatteryCapacity(20);
        droneRepository.save(drone);

        // Change State using controller get method
        mockMvc.perform(MockMvcRequestBuilders
                .get(url + "/change_state/" + drone.getSerialNumber() + "/LOADING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json

        // Change State using controller Post Method
        ChangeStateRequest req = new ChangeStateRequest();
        req.setDrone(drone);
        req.setState(DroneStateOptions.LOADING);

        String jsonData = mapper.writeValueAsString(req);
        mockMvc.perform(MockMvcRequestBuilders.post(url + "/change_state")
                .content(jsonData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType)); // expect errors also in json
    }

    @Test
    void whenGettingDronesAvailableForLoading_thenReturnOnlyDronesInLoadingOrIdleStateWithBatteryGreaterThan24() throws Exception {
        Drones drone1 = droneRepository.findAll().get(0);
        Drones drone2 = droneRepository.findAll().get(1);
        Drones drone3 = droneRepository.findAll().get(2);
        Drones drone4 = droneRepository.findAll().get(3);

        // set drone1 ready for loading
        drone1.setState(DroneStateOptions.LOADING);
        drone1.setBatteryCapacity(75);
        droneRepository.save(drone1);

        // set drone2 ready with IDLE state
        drone2.setState(DroneStateOptions.IDLE);
        drone2.setBatteryCapacity(100);
        droneRepository.save(drone2);

        // set drone3 with Low Battery
        drone3.setState(DroneStateOptions.IDLE);
        drone3.setBatteryCapacity(20);
        droneRepository.save(drone3);

        // set drone4 with Low Battery
        drone4.setState(DroneStateOptions.LOADED);
        drone4.setBatteryCapacity(80);
        droneRepository.save(drone4);

        // Get available drones from controller
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url + "/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        // response does contains drone1 and drone 2
        assertTrue(result.getResponse().getContentAsString().contains(drone1.getSerialNumber()));
        assertTrue(result.getResponse().getContentAsString().contains(drone2.getSerialNumber()));

        // response does not contain drone 3 & 4
        assertFalse(result.getResponse().getContentAsString().contains(drone3.getSerialNumber()));
        assertFalse(result.getResponse().getContentAsString().contains(drone4.getSerialNumber()));
    }

    @Test
    void whenCheckingCurrentBatteryCapacity_thenReturnBatteryCapacityPercentageInNumbers() throws Exception {
        Drones drone = droneRepository.findAll().get(0);

        // Get available drones from controller
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/check_battery/" + drone.getSerialNumber())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(drone.getBatteryCapacity())));
    }
}