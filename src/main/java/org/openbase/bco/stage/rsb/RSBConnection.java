package org.openbase.bco.stage.rsb;

/*
 * #%L
 * BCO Visual Stage
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
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.util.Properties;
import rst.tracking.PointingRay3DFloatCollectionType;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class RSBConnection {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    private Listener skeletonListener;
    private Listener rayListener;
    
    public RSBConnection(AbstractEventHandler handler) throws CouldNotPerformException {
        initializeListener(handler);
    }
    
    public void deactivate() throws CouldNotPerformException{
        try{
            skeletonListener.deactivate();
            rayListener.deactivate();
        } catch (RSBException | InterruptedException ex) {
            throw new CouldNotPerformException("Could not deactivate informer and listener.", ex);
        } 
    }
    
    private void initializeListener(AbstractEventHandler handler) throws CouldNotPerformException{
        final ProtocolBufferConverter<TrackedPostures3DFloat> postureConverter = new ProtocolBufferConverter<>(
                    TrackedPostures3DFloat.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(postureConverter);
        
        final ProtocolBufferConverter<PointingRay3DFloatCollectionType.PointingRay3DFloatCollection> rayConverter = new ProtocolBufferConverter<>(
                    PointingRay3DFloatCollectionType.PointingRay3DFloatCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(rayConverter);
        
        
        try {
            if(JPService.getProperty(JPLocalInput.class).getValue()){
                skeletonListener = Factory.getInstance().createListener(JPService.getProperty(JPPostureScope.class).getValue(), getLocalConfig());
                rayListener = Factory.getInstance().createListener(JPService.getProperty(JPRayScope.class).getValue(), getLocalConfig());
            } else {
                skeletonListener = Factory.getInstance().createListener(JPService.getProperty(JPPostureScope.class).getValue());
                rayListener = Factory.getInstance().createListener(JPService.getProperty(JPRayScope.class).getValue());
            }
            skeletonListener.activate();
            rayListener.activate();
            
            // Add an EventHandler.
            skeletonListener.addHandler(handler, true);
            rayListener.addHandler(handler, true);
            
        } catch (JPNotAvailableException | RSBException | InterruptedException ex) {
            throw new CouldNotPerformException("RSB listener could not be initialized.", ex);
        }
    }

    private ParticipantConfig getLocalConfig() {
        ParticipantConfig localConfig = Factory.getInstance().getDefaultParticipantConfig().copy();
        Properties localProperties = new Properties();
        localProperties.setProperty("transport.socket.host", "localhost");
        for (TransportConfig tc : localConfig.getTransports().values()) {
            tc.setEnabled(false);
        }
        localConfig.getOrCreateTransport("socket").setEnabled(true);
        localConfig.getOrCreateTransport("socket").setOptions(localProperties);
        return localConfig;
    }   
}
