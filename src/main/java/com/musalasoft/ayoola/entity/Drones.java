package com.musalasoft.ayoola.entity;

import com.musalasoft.ayoola.dto.DroneModelOptions;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import jakarta.persistence.*;
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
    @Id
    @Column(length = 100)
    private String serialNumber;

    @Column
    private float weight;

    @Column
    private int batteryCapacity;

    @Column
    @Enumerated(EnumType.STRING)
    private DroneModelOptions model;

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
