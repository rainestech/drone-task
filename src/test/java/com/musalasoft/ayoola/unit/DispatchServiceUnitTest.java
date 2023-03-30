package com.musalasoft.ayoola.unit;

import com.musalasoft.ayoola.dto.ChangeStateRequest;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.dto.LoadMedicationRequest;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.repository.DroneRepository;
import com.musalasoft.ayoola.repository.MedicationRepository;
import com.musalasoft.ayoola.services.DispatchService;
import com.musalasoft.ayoola.util.PopulateSampleData;
import com.musalasoft.ayoola.util.exceptions.DroneNotInLoadableStateException;
import com.musalasoft.ayoola.util.exceptions.LowBatteryException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DispatchServiceUnitTest {

    @Autowired
    DispatchService service;

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    MedicationRepository medicationRepository;

    @Autowired
    PopulateSampleData sampleData;

    @BeforeAll
    public void setup() {
        sampleData.populateDrones();
        sampleData.populateMedication();
        sampleData.loadItems();
    }

    @Test
    void testRegisterDrone() {
        Drones drone = droneRepository.findAll().get(0);

        assertThrows(ResponseStatusException.class, () -> service.registerDrone(drone));

        drone.setSerialNumber("NEWSERIALNUMBER");
        service.registerDrone(drone);

        assertTrue(droneRepository.findById(drone.getSerialNumber()).isPresent());
    }

    @Test
    void testGetLoadedItems() {
        List<Drones> loadedDrones = droneRepository.findByLoadedMedicationsIsNotNull();

        assertEquals(loadedDrones.get(0).getLoadedMedications().size(),
                service.getLoadedItems(loadedDrones.get(0).getSerialNumber()).size());

        assertTrue(loadedDrones.get(0).getLoadedMedications().stream().map(Medications::getCode).toList()
                .contains(service.getLoadedItems(loadedDrones.get(0)).get(0).getCode()));
    }

    @Test
    void testLoadMedications() {
        Drones drone = droneRepository.findAll().get(0);
        Medications med = medicationRepository.findAll().get(0);

        // ensure drone is empty
        drone.setLoadedMedications(new ArrayList<>());
        drone.setBatteryCapacity(100);
        drone.setState(DroneStateOptions.RETURNING);
        droneRepository.save(drone);

        LoadMedicationRequest req = new LoadMedicationRequest();
        req.setDrone(drone);
        req.setMedication(med);
        req.setQuantity(1);

        // test drone can not be loaded when not in loading state
        assertThrows(DroneNotInLoadableStateException.class, () -> service.loadMedication(req));

        drone.setState(DroneStateOptions.LOADING);
        droneRepository.save(drone);

        // test drones can not be overloaded
        assertThrows(ResponseStatusException.class,
                () -> service.loadMedication(drone.getSerialNumber(), med.getCode(), 100));

        // now properly load drone and test
        assertEquals(med,
                service.getLoadedItems(service.loadMedication(req)).get(0));

        assertTrue(droneRepository.findByLoadedMedicationsIsNotNull()
                .stream().anyMatch(d -> d.getSerialNumber().equals(drone.getSerialNumber())));
    }

    @Test
    void testChangeState() {
        Drones drone = droneRepository.findAll().get(0);

        // test when battery capacity is below 25
        drone.setBatteryCapacity(24);
        drone.setState(DroneStateOptions.IDLE);
        droneRepository.save(drone);

        // test drone cannot be put into loading state while battery is low
        assertThrows(LowBatteryException.class,
                () -> service.changeState(drone.getSerialNumber(), DroneStateOptions.LOADING));

        // test that drone state can be changed to other states except LOADING when battery is below 25% capacity
        assertEquals(DroneStateOptions.RETURNING,
                service.changeState(drone.getSerialNumber(), DroneStateOptions.RETURNING)
                        .getState());

        drone.setBatteryCapacity(25);
        droneRepository.save(drone);
        ChangeStateRequest req = new ChangeStateRequest();
        req.setState(DroneStateOptions.LOADING);
        req.setDrone(drone);

        // drone state can be changed to loading at 25% battery capacity and above
        assertEquals(DroneStateOptions.LOADING, service.changeState(req).getState());
    }

    @Test
    void testGetAvailableDronesForLoading() {
        List<Drones> dronesForLoading = service.getAvailableDronesForLoading();

        if (dronesForLoading.size() > 0) {
            assertFalse(dronesForLoading.stream()
                    .anyMatch(l -> l.getState().equals(DroneStateOptions.RETURNING)));

            assertFalse(dronesForLoading.stream()
                    .anyMatch(l -> l.getState().equals(DroneStateOptions.DELIVERED)));

            assertFalse(dronesForLoading.stream()
                    .anyMatch(l -> l.getState().equals(DroneStateOptions.LOADED)));

            assertFalse(dronesForLoading.stream()
                    .anyMatch(l -> l.getWeight() > 500));

            assertFalse(dronesForLoading.stream()
                    .anyMatch(l -> l.getBatteryCapacity() < 25));
        }
    }
}
