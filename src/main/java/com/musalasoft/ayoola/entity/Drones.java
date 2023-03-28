package com.musalasoft.ayoola.entity;

import com.musalasoft.ayoola.dto.DroneModelOptions;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "drones")
@Getter
@Setter
@RequiredArgsConstructor
public class Drones {
    @NotEmpty
    @Id
    @Column(length = 100)
    private String serialNumber;

    @Min(value = 0, message = "Weight must be a positive number in grams")
    @Max(value = 500, message = "Maximum weight is 500 grams")
    @Column
    private float weight;

    @Min(value = 0, message = "Battery capacity in percentage must be a positive integer")
    @Max(value = 100, message = "Maximum Battery Capacity is 100%")
    @Column
    private int batteryCapacity;

    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private DroneModelOptions model;

    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private DroneStateOptions state;

    @ManyToMany
    @JoinTable(
            name = "drone_loaded_medications",
            joinColumns = {@JoinColumn(name = "droneSn", referencedColumnName = "serialNumber")},
            inverseJoinColumns = {@JoinColumn(name = "medicationCode", referencedColumnName = "code")})
    private List<Medications> loadedMedications;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Drones drones = (Drones) o;
        return serialNumber != null && Objects.equals(serialNumber, drones.serialNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
