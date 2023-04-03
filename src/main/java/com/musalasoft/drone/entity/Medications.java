package com.musalasoft.drone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "medications")
@Getter
@Setter
@RequiredArgsConstructor
public class Medications {
    @Id
    @Pattern(regexp = ".*[A-Z0-9_]$", message = "Invalid Medication code! only upper case letters, underscore and numbers are allowed")
    private String code;

    @Pattern(regexp = ".*[A-Za-z0-9-_]$", message = "Invalid Medication name! only alphanumeric characters, hyphen, underscore and numbers are allowed")
    @Column
    private String name;

    @NotEmpty
    @Column
    private String imageUrl;

    @Min(value = 0, message = "Medication weight must be a positive number in grams")
    @Max(value = 500, message = "Medications weight exceeds maximum grams deliverable by drones")
    @Column
    private float weight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Medications that = (Medications) o;
        return code != null && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
