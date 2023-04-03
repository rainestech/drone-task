package com.musalasoft.drone.dto;

import com.musalasoft.drone.entity.Drones;
import lombok.Data;

@Data
public class ChangeStateRequest {
    private Drones drone;
    private DroneStateOptions state;
}
