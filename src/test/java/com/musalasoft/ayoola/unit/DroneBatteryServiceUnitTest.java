package com.musalasoft.ayoola.unit;

import com.musalasoft.ayoola.entity.DroneBatteryEventLog;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.repository.DroneBatteryRepository;
import com.musalasoft.ayoola.repository.DroneRepository;
import com.musalasoft.ayoola.services.DroneBatteryService;
import com.musalasoft.ayoola.util.PopulateSampleData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DroneBatteryServiceUnitTest {

    @Autowired
    DroneBatteryService service;

    @Autowired
    DroneBatteryRepository repository;

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    PopulateSampleData sampleData;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
        sampleData.populateMedication();
    }

    @Test
    void testGetDroneBatteryEvent() {
        Drones drone = droneRepository.findAll().get(0);
        DroneBatteryEventLog log1 = new DroneBatteryEventLog(drone, 50);
        DroneBatteryEventLog log2 = new DroneBatteryEventLog(drone, 60);
        DroneBatteryEventLog log3 = new DroneBatteryEventLog(drone, 40);

        repository.save(log1);
        repository.save(log2);
        repository.save(log3);

        // as the Battery monitor service might have started, the minimum number of
        // records will be the ones just added
        assertTrue(service.getBatteryEvent().size() >= 3);
    }

    @Test
    void testSaveDroneEvent() {
        Drones drone = droneRepository.findAll().get(0);
        DroneBatteryEventLog log1 = new DroneBatteryEventLog(drone, 50);
        service.saveEvent(log1);

        assertTrue(repository.findAll().stream().anyMatch(l -> l.equals(log1)));
    }

    @Test
    void testGetDroneEventLog() {
        Drones drone = droneRepository.findAll().get(0);

        assertTrue(service.getDronesEventLog(drone.getSerialNumber()).size() > 0);
    }
}
