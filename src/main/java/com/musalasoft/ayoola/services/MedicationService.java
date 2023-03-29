package com.musalasoft.ayoola.services;

import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.repository.MedicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final DroneService droneService;

    public MedicationService(MedicationRepository repository, DroneService droneService) {
        this.repository = repository;
        this.droneService = droneService;
    }

    /* Check if record of the requested Medication exists in the database
     *
     * @param data request Medication
     * @return boolean
     */
    public boolean medicationExists(Medications data) {
        return repository.findById(data.getCode()).isPresent();
    }

    /* Get the records of Medications available in the database
     *
     * @param data request Medication
     * @return List of Medications
     */
    public List<Medications> getMedications() {
        return repository.findAll();
    }

    /* Get the records of Medications available in the database
     *
     * @param data request Medication
     * @return List of Medications
     */
    @Nullable
    public Medications getMedication(String code) {
        return repository.findById(code)
                .orElse(null);
    }

    /* Save / edit Medication record
     *
     * @param data request Medication
     * @return saved / updated Medications
     */
    public Medications saveMedication(Medications data) {
        return repository.save(data);
    }

    /* Remove Medications record from the database
     *
     * @param data request Medication Code
     *
     * @throws ResponseStatusException
     * @return removed Medication Record
     */
    public Medications deleteMedication(String medicationCode) {
        return deleteMedication(medicationCode, false);
    }

    public Medications deleteMedication(String medicationCode, boolean force) {
        Medications data = repository.findById(medicationCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Medication with code: " + medicationCode + " not found"));

        if (force)
            droneService.unloadMedication(data);

        repository.delete(data);
        return data;
    }
}
