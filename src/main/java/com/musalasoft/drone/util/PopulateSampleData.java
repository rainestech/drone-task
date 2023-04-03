package com.musalasoft.drone.util;

import com.github.javafaker.Faker;
import com.musalasoft.drone.dto.DroneModelOptions;
import com.musalasoft.drone.dto.DroneStateOptions;
import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.entity.Medications;
import com.musalasoft.drone.repository.DroneRepository;
import com.musalasoft.drone.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PopulateSampleData {
    private final DroneRepository droneRepository;
    private final MedicationRepository medicationRepository;

    private final Random random = new Random();
    private final Faker faker = new Faker();


    public PopulateSampleData(DroneRepository droneRepository, MedicationRepository medicationRepository) {
        this.droneRepository = droneRepository;
        this.medicationRepository = medicationRepository;
    }

    public void loadItems() {
        List<Drones> drones = droneRepository.findAll();
        List<Medications> medications = medicationRepository.findAll();

        for (int i = 0; i < 5; i++) {
            Drones drone = drones.get(random.nextInt(drones.size()));
            List<Medications> m = new ArrayList<>();
            m.add(medications.get(random.nextInt(medications.size())));
            drone.setLoadedMedications(m);

            droneRepository.save(drone);
        }
    }

    public void populateDrones() {
        for (int i = 0; i < 10; i++) {
            Drones d = new Drones();
            d.setBatteryCapacity(random.nextInt(100));
            d.setState(randomState());
            d.setModel(randomModel());
            d.setWeight(0);
            d.setSerialNumber(faker.bothify("????_????_????_????-####"));

            droneRepository.save(d);
        }
    }

    public void populateMedication() {
        for (int i = 0; i < 10; i++) {
            Medications m = new Medications();
            m.setWeight(random.nextInt(200));
            m.setCode(faker.bothify("????_????_????_????_####", true));
            m.setImageUrl("https://fake-image.com/" + m.getCode());
            m.setName(faker.bothify("?????#_?????#_?????#_?????#"));

            medicationRepository.save(m);
        }
    }

    private DroneModelOptions randomModel() {
        DroneModelOptions[] options = DroneModelOptions.values();

        return options[random.nextInt(options.length)];
    }

    private DroneStateOptions randomState() {
        DroneStateOptions[] options = DroneStateOptions.values();

        return options[random.nextInt(options.length)];
    }
}
