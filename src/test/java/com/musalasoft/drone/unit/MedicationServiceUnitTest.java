package com.musalasoft.drone.unit;

import com.musalasoft.drone.entity.Medications;
import com.musalasoft.drone.repository.MedicationRepository;
import com.musalasoft.drone.services.MedicationService;
import com.musalasoft.drone.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicationServiceUnitTest {

    @Autowired
    MedicationRepository medicationRepository;

    @Autowired
    MedicationService service;

    @Autowired
    PopulateSampleData sampleData;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
        sampleData.populateMedication();
    }

    @Test
    void testGetMedication() {
        Medications m = medicationRepository.findAll().get(0);

        Medications serviceMed = service.getMedication(m.getCode());
        assertEquals(m, serviceMed);
    }

    @Test
    void testMedicationExists() {
        Medications m = medicationRepository.findAll().get(0);

        assertTrue(service.medicationExists(m));
    }

    @Test
    void testGetMedications() {
        List<Medications> medications = medicationRepository.findAll();

        assertEquals(medications.size(), service.getMedications().size());
    }

    @Test
    void testSaveMedication() {
        Medications m = new Medications();
        m.setName("SAMPLENAME");
        m.setImageUrl("https://web.com");
        m.setCode("SAMPLECODE");
        m.setWeight(400);

        service.saveMedication(m);

        assertTrue(medicationRepository.findById("SAMPLECODE").isPresent());
    }

    @Test
    void testDeleteMedications() {
        Medications m = medicationRepository.findAll().get(0);

        service.deleteMedication(m.getCode(), true);
        assertTrue(medicationRepository.findById(m.getCode()).isEmpty());
    }
}
