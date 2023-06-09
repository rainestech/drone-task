package com.musalasoft.drone.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.drone.MedicationController;
import com.musalasoft.drone.dto.DroneStateOptions;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class MedicationControllerIntegrationTest {
    private final MediaType jsonMediaType = MediaType.APPLICATION_JSON;
    private final String url = "/api/v1/medications";
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MedicationController controller;

    @Autowired
    MedicationRepository medicationRepository;

    @Autowired
    DroneRepository droneRepository;

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
    void whenGettingListOfMedications_thenReturnListOfMedicationsAsJsonResponse() throws Exception {
        // Get the list from database and compare the response
        List<Medications> meds = medicationRepository.findAll();

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(meds)));
    }

    @Test
    void whenSavingANewMedicationWithInvalidNameOrCode_thenReturn4XXClientError() throws Exception {
        // code (allowed only upper case letters, underscore and numbers);
        String dataInvalidCode = """
                {
                    "code": "TEST_DATA_medications",
                    "name": "TEST_MEDICATION_NAME",
                    "imageUrl": "https://imageplaceholder.com/image1",
                    "weight": 200
                }""";

        // name (allowed only letters, numbers, ‘-‘, ‘_’);
        String dataInvalidName = """
                {
                    "code": "TEST_DATA_MED_123",
                    "name": "TEST_MEDICATION-NAME-@",
                    "imageUrl": "https://imageplaceholder.com/image1",
                    "weight": 200
                }""";

        MvcResult resultCode = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(dataInvalidCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        MvcResult resultName = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(dataInvalidName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andReturn();

        // response contains error message
        assertTrue(resultCode.getResponse().getContentAsString().contains("Invalid Medication code!"));
        assertTrue(resultName.getResponse().getContentAsString().contains("Invalid Medication name!"));
    }

    @Test
    void whenSavingNewMedication_thenReturnSavedMedicationData() throws Exception {
        String data = """
                {
                    "code": "TEST_DATA_MED_456",
                    "name": "TEST_MEDICATION_NAME",
                    "imageUrl": "https://imageplaceholder.com/image1",
                    "weight": 200
                }""";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(data));

        assertTrue(medicationRepository.findById("TEST_DATA_MED_456").isPresent());
    }

    @Test
    void whenUpdatingMedication_thenReturnUpdatedMedicationData() throws Exception {
        Medications med = medicationRepository.findAll().get(2);
        med.setCode("TEST_DATA_MED_789");
        med.setWeight(300);

        // save init
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(mapper.writeValueAsString(med))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(med)));

        assertEquals(300, medicationRepository.findById("TEST_DATA_MED_789").get().getWeight());
    }

    @Test
    void whenDeleteLoadedMedicationWithOutForce_thenReturn4XXClient() throws Exception {
        Medications med = medicationRepository.findAll().get(0);
        Drones drone = droneRepository.findAll().get(0);

        List<Medications> medToLoad = new ArrayList<>();
        medToLoad.add(med);

        drone.setState(DroneStateOptions.LOADING);
        drone.setBatteryCapacity(100);
        drone.setLoadedMedications(medToLoad);

        droneRepository.save(drone);

        // save init
        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/remove/" + med.getCode())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));
    }

    @Test
    void whenDeleteLoadedMedicationWithForce_thenReturnDeletedMedication() throws Exception {
        Medications med = medicationRepository.findAll().get(0);
        Drones drone = droneRepository.findAll().get(0);

        List<Medications> medToLoad = new ArrayList<>();
        medToLoad.add(med);

        drone.setState(DroneStateOptions.LOADING);
        drone.setBatteryCapacity(100);
        drone.setLoadedMedications(medToLoad);

        droneRepository.save(drone);

        // save init
        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/remove/" + med.getCode() + "/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(med)));

        // confirm record is off the database
        assertTrue(medicationRepository.findById(med.getCode()).isEmpty());
    }

    @Test
    void whenTryingToDeleteNonExistingRecord_thenThrow4XXClientError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/remove/MED_404")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(jsonMediaType));

    }
}
