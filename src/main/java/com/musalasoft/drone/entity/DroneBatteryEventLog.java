package com.musalasoft.drone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "drones_battery_event_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DroneBatteryEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "droneSn")
    private Drones drone;

    @Column
    private Integer batteryPercentage;

    @Column
    @CreationTimestamp
    private Timestamp createdAt;

    public DroneBatteryEventLog(Drones drone, Integer batteryPercentage) {
        this.drone = drone;
        this.batteryPercentage = batteryPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DroneBatteryEventLog that = (DroneBatteryEventLog) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
