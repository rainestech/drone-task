package com.musalasoft.ayoola.unit;

import com.musalasoft.ayoola.dto.DroneModelOptions;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.repository.DroneRepository;
import com.musalasoft.ayoola.services.DroneService;
import com.musalasoft.ayoola.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DroneServiceUnitTest {

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    DroneService service;

    @Autowired
    PopulateSampleData sampleData;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
        sampleData.populateMedication();
        sampleData.loadItems();
    }

    @Test
    void testGetDrone() {
        Drones drone = droneRepository.findAll().get(0);

        Drones serviceDrone = service.getDrone(drone.getSerialNumber());
        assertEquals(drone, serviceDrone);
    }

    @Test
    void testGetListOfDronesByState() {
        DroneStateOptions state = DroneStateOptions.LOADING;

        List<Drones> serviceDrones = service.getListOfDronesByState(state);
        assertTrue(serviceDrones.stream()
                .filter(d -> d.getState().equals(DroneStateOptions.IDLE))
                .findFirst().isEmpty());

        assertTrue(serviceDrones.stream()
                .anyMatch(d -> d.getState().equals(state)));
    }

    @Test
    void testDroneExists() {
        Drones drone = droneRepository.findAll().get(0);

        assertTrue(service.droneExists(drone));
    }

    @Test
    void testGetDrones() {
        List<Drones> drones = droneRepository.findAll();

        assertEquals(drones.size(), service.getDrones().size());
    }

    @Test
    void testSaveDrone() {
        Drones drone = new Drones();
        drone.setWeight(0);
        drone.setState(DroneStateOptions.LOADING);
        drone.setModel(DroneModelOptions.Cruiserweight);
        drone.setBatteryCapacity(100);
        drone.setSerialNumber("TESTDATA-1234");

        service.saveDrone(drone);

        assertTrue(droneRepository.findById("TESTDATA-1234").isPresent());
    }

    @Test
    void testDeleteDrone() {
        Drones drone = new Drones();
        drone.setWeight(0);
        drone.setState(DroneStateOptions.LOADING);
        drone.setModel(DroneModelOptions.Cruiserweight);
        drone.setBatteryCapacity(100);
        drone.setSerialNumber("TESTDATA-1234");

        droneRepository.save(drone);

        service.deleteDrone("TESTDATA-1234");
        assertTrue(droneRepository.findById("TESTDATA-1234").isEmpty());
    }

    @Test
    void testUnloadMedication() {
        Drones drone = droneRepository.findByLoadedMedicationsIsNotNull().get(0);
        Medications med = drone.getLoadedMedications().get(0);

        service.unloadMedication(med);

        assertFalse(droneRepository.findByLoadedMedicationsIsNotNull().stream()
                .anyMatch(d -> d.getLoadedMedications().stream()
                        .anyMatch(m -> m.getCode().equals(med.getCode()))));
    }
}
