package com.musalasoft.drone.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class EntityInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(EntityInitializer.class);
    private final PopulateSampleData sampleData;
    private boolean alreadySetup;
    @Value("${sample.data.populate:false}")
    private boolean populateData;

    public EntityInitializer(PopulateSampleData sampleData) {
        this.sampleData = sampleData;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup || !populateData)
            return;

        sampleData.populateMedication();
        sampleData.populateDrones();
        sampleData.loadItems();
        alreadySetup = true;
        logger.info("Sample Data Populated Successfully");
    }
}
