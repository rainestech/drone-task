package com.musalasoft.ayoola.dto;

import com.musalasoft.ayoola.entity.Drones;
import lombok.Data;

@Data
public class ChangeStateRequest {
    private Drones drone;
    private DroneStateOptions state;
}
