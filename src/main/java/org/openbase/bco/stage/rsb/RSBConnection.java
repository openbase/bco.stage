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
import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.bco.psc.lib.rsb.LocalConfigProviderInterface;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection implements LocalConfigProviderInterface {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    private final AbstractEventHandler handler;

    /**
     * RSB Listener used to receive posture events.
     */
    private Listener skeletonListener;
    /**
     * RSB Listener used to receive pointing ray events.
     */
    private Listener rayListener;
    /**
     * RSB Listener used to receive selected unit events.
     */
    private Listener selectedUnitListener;

    public RSBConnection(AbstractEventHandler handler) {
        this.handler = handler;
    }

    /**
     * Initializes the rsb connection.
     *
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * class fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    public void init() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Initializing RSB connection.");
        initializeListeners(handler);
    }

    /**
     * Deactivates the RSB connection.
     *
     * @throws CouldNotPerformException is thrown, if the deactivation fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating RSB connection.");
        try {
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
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * Listeners fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    private void initializeListeners(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Registering converters.");
        final ProtocolBufferConverter<TrackedPostures3DFloat> postureConverter = new ProtocolBufferConverter<>(
                TrackedPostures3DFloat.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(postureConverter);

        final ProtocolBufferConverter<PointingRay3DFloatDistributionCollection> rayConverter = new ProtocolBufferConverter<>(
                PointingRay3DFloatDistributionCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(rayConverter);

        final ProtocolBufferConverter<UnitProbabilityCollection> selectedUnitConverter = new ProtocolBufferConverter<>(
                UnitProbabilityCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(selectedUnitConverter);

        try {
            Scope baseScope = JPService.getProperty(JPPSCBaseScope.class).getValue();
            Scope postureScope = baseScope.concat(JPService.getProperty(JPPostureScope.class).getValue());
            Scope rayScope = baseScope.concat(JPService.getProperty(JPRayScope.class).getValue());
            Scope selectedUnitScope = baseScope.concat(JPService.getProperty(JPSelectedUnitScope.class).getValue());
            LOGGER.info("Initializing RSB Posture Listener on scope: " + postureScope);
            LOGGER.info("Initializing RSB Ray Listener on scope: " + rayScope);
            LOGGER.info("Initializing RSB Selected Unit Listener on scope: " + selectedUnitScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
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
}
