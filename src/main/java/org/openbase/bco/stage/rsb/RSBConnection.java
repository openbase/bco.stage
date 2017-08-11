package org.openbase.bco.stage.rsb;

/*
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.stage.jp.JPPostureScope;
import org.openbase.bco.stage.jp.JPLocalInput;
import org.openbase.bco.stage.jp.JPRayScope;
import org.openbase.bco.stage.jp.JPSelectedUnitScope;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.util.Properties;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection {
    /** Logger instance. */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    /** RSB Listener used to receive posture events. */
    private Listener skeletonListener;
    /** RSB Listener used to receive pointing ray events. */
    private Listener rayListener;
    /** RSB Listener used to receive selected unit events. */
    private Listener selectedUnitListener;
    
    /**
     * Constructor.
     * 
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the class fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public RSBConnection(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Initializing RSB connection.");
        initializeListeners(handler);
    }
    
    /**
     * Deactivates the RSB connection.
     * 
     * @throws CouldNotPerformException is thrown, if the deactivation fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public void deactivate() throws CouldNotPerformException, InterruptedException{
        LOGGER.info("Deactivating RSB connection.");
        try{
            skeletonListener.deactivate();
            rayListener.deactivate();
            selectedUnitListener.deactivate();
        } catch (RSBException ex) {
            throw new CouldNotPerformException("Could not deactivate informer and listener.", ex);
        } 
    }
    
    /**
     * Initializes the RSB Listeners.
     * 
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the Listeners fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    private void initializeListeners(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException{
        LOGGER.debug("Registering converters.");
        final ProtocolBufferConverter<TrackedPostures3DFloat> postureConverter = new ProtocolBufferConverter<>(
                    TrackedPostures3DFloat.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(postureConverter);
        
        final ProtocolBufferConverter<PointingRay3DFloatCollection> rayConverter = new ProtocolBufferConverter<>(
                    PointingRay3DFloatCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(rayConverter);
        
        final ProtocolBufferConverter<UnitProbabilityCollection> selectedUnitConverter = new ProtocolBufferConverter<>(
                    UnitProbabilityCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(selectedUnitConverter);
        
        
        try {
            Scope postureScope = JPService.getProperty(JPPostureScope.class).getValue();
            Scope rayScope = JPService.getProperty(JPRayScope.class).getValue();
            Scope selectedUnitScope = JPService.getProperty(JPSelectedUnitScope.class).getValue();
            LOGGER.info("Initializing RSB Posture Listener on scope: " + postureScope);
            LOGGER.info("Initializing RSB Ray Listener on scope: " + rayScope);
            LOGGER.info("Initializing RSB Selected Unit Listener on scope: " + selectedUnitScope);
            if(JPService.getProperty(JPLocalInput.class).getValue()){
                LOGGER.warn("RSB input set to socket and localhost.");
                skeletonListener = Factory.getInstance().createListener(postureScope, getLocalConfig());
                rayListener = Factory.getInstance().createListener(rayScope, getLocalConfig());
                selectedUnitListener = Factory.getInstance().createListener(selectedUnitScope, getLocalConfig());
            } else {
                skeletonListener = Factory.getInstance().createListener(postureScope);
                rayListener = Factory.getInstance().createListener(rayScope);
                selectedUnitListener = Factory.getInstance().createListener(selectedUnitScope);
            }
            skeletonListener.activate();
            rayListener.activate();
            selectedUnitListener.activate();
            
            // Add an EventHandler.
            skeletonListener.addHandler(handler, true);
            rayListener.addHandler(handler, true);
            selectedUnitListener.addHandler(handler, true);
            
        } catch (JPNotAvailableException | RSBException ex) {
            throw new CouldNotPerformException("RSB listener could not be initialized.", ex);
        }
    }

    /**
     * Creates an RSB configuration for connecting via socket and localhost.
     * 
     * @return the local communication configuration.
     */
    private ParticipantConfig getLocalConfig() {
        ParticipantConfig localConfig = Factory.getInstance().getDefaultParticipantConfig().copy();
        Properties localProperties = new Properties();
        localProperties.setProperty("transport.socket.host", "localhost");
        localConfig.getTransports().values().forEach((tc) -> {
            tc.setEnabled(false);
        });
        localConfig.getOrCreateTransport("socket").setEnabled(true);
        localConfig.getOrCreateTransport("socket").setOptions(localProperties);
        return localConfig;
    }
}
