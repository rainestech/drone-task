package com.musalasoft.drone.services;

import com.musalasoft.drone.entity.DroneBatteryEventLog;
import com.musalasoft.drone.entity.Drones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Random;

@Component
public class BatteryMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(BatteryMonitorService.class);
    private final DroneBatteryService batteryService;
    private final DroneService droneService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d M yyyy H:m:s");
    private final Random random = new Random();

    public BatteryMonitorService(DroneBatteryService service, DroneService droneService) {
        this.batteryService = service;
        this.droneService = droneService;
    }

    @Scheduled(fixedDelay = 1000)
    public void droneBatteryMonitorStub() {
        for (Drones drone : droneService.getDrones()) {
            try {
                drone.setBatteryCapacity(getDroneBatteryStatus(drone));

                DroneBatteryEventLog batteryEventLog = new DroneBatteryEventLog(drone, drone.getBatteryCapacity());
                batteryService.saveEvent(batteryEventLog);

                String consoleLog = String.format("%s (%s) is at %s as at %s",
                        drone.getModel(), drone.getSerialNumber(),
                        batteryEventLog.getBatteryPercentage() + "%",
                        dateFormat.format(batteryEventLog.getCreatedAt()));
                logger.info(consoleLog);

                droneService.saveDrone(drone);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    private Integer getDroneBatteryStatus(Drones drone) {
        switch (drone.getState()) {
            // assume drones are fully charged when Idle
            case IDLE -> {
                return 100;
            }

            // assume the battery is within the range of 25 and 100 when loading / loaded
            case LOADING, LOADED -> {
                return batteryLevel(50, 100);
            }

            // assume the battery capacity is with the range of 25% - 50% as at the time of delivery
            case DELIVERED -> {
                return batteryLevel(25, 50);
            }

            // simulate battery draining as the drone is delivering / returning
            default -> {
                if (drone.getBatteryCapacity() < 20)
                    return drone.getBatteryCapacity();

                return drone.getBatteryCapacity() - 2;
            }
        }
    }

    private Integer batteryLevel(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
