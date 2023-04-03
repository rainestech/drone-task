package com.musalasoft.drone.util.exceptions;

public class LowBatteryException extends DroneRuntimeException {
    public LowBatteryException(String message) {
        super(message);
    }
}
