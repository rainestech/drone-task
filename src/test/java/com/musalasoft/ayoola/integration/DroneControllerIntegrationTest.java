package com.musalasoft.ayoola.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.ayoola.DroneController;
import com.musalasoft.ayoola.dto.DroneModelOptions;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.repository.DroneRepository;
import com.musalasoft.ayoola.util.PopulateSampleData;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class DroneControllerIntegrationTest {
    private final MediaType jsonMediaType = MediaType.APPLICATION_JSON;
    @Autowired
    DroneController droneController;
    @Autowired
    DroneRepository droneRepository;
    @Autowired
    PopulateSampleData sampleData;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenDroneControllerIndex_thenReturnStatusOkAndJsonContent() throws Exception {
        sampleData.populateDrones();
        List<Drones> data = droneRepository.findAll();

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(data);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/drones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(jsonData));
    }

    @Test
    void whenSaveNewDrone_thenNewDronePersistSuccessfulAndReturned() throws Exception {
        String data = """
                {
                        "serialNumber": "TESTDATA",
                        "weight": 0.0,
                        "batteryCapacity": 85,
                        "model": "Cruiserweight",
                        "state": "RETURNING",
                        "loadedMedications": []
                    }""";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/drones")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(data))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/drones/sn/TESTDATA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(data))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));
    }

    @Test
    void whenSavingDroneWithWeight_thenServerIgnoreClientWeightInput() throws Exception {
        String data = """
                {
                        "serialNumber": "TESTDATAWEIGHT",
                        "weight": 45,
                        "batteryCapacity": 85,
                        "model": "Cruiserweight",
                        "state": "RETURNING",
                        "loadedMedications": []
                    }""";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/drones")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        assertNotEquals(result.getResponse().getContentAsString(), data);
    }

    @Test
    void whenSavingDuplicateDrone_thenThrowExceptionWithStatus4XXAndMessage() throws Exception {
        String data = """
                {
                        "serialNumber": "TESTDATA2",
                        "weight": 0.0,
                        "batteryCapacity": 85,
                        "model": "Cruiserweight",
                        "state": "RETURNING",
                        "loadedMedications": []
                    }""";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/drones")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON));

        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/drones")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        assertTrue(res.getResponse().getContentAsString().contains("Drone exists in the database"));
    }

    @Test
    void whenEditDrone_thenUpdateRecordWithoutException() throws Exception {
        Drones data = new Drones();
        data.setState(DroneStateOptions.LOADING);
        data.setSerialNumber("TESTDATA3");
        data.setModel(DroneModelOptions.Cruiserweight);
        data.setLoadedMedications(new ArrayList<>()); // sets weight to zero since medications is an empty array

        ObjectMapper mapper = new ObjectMapper();
        droneRepository.save(data);

        // update test data
        data.setBatteryCapacity(50);
        data.setState(DroneStateOptions.DELIVERED);
        String jsonData = mapper.writeValueAsString(data);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/drones")
                .content(jsonData)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(jsonData))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        // fetching test data matches the new update
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/drones/sn/TESTDATA3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(jsonData))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();
    }

    @Test
    void whenDeleteDrone_thenRecordRemoveSuccessIfFound() throws Exception {
        String data = """
                {
                        "serialNumber": "TESTDATA4",
                        "weight": 0.0,
                        "batteryCapacity": 85,
                        "model": "Cruiserweight",
                        "state": "RETURNING",
                        "loadedMedications": []
                    }""";

        // save data for test
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/drones")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON));

        // delete saved data
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/drones/remove/TESTDATA4")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));

        // try to delete data again which should throw not found exception (status 404)
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/drones/remove/TESTDATA4")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        // trying to fetch deleted record also result in 404 error
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/drones/sn/TESTDATALONGERTHANSAMPLE4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

}
