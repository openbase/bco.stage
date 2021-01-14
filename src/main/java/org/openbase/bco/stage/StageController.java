package org.openbase.bco.stage;

/*
 * -
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 - 2021 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import javafx.application.Platform;
import javafx.stage.Stage;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.stage.jp.JPDisableRegistry;
import org.openbase.bco.stage.jp.JPFilterPscUnits;
import org.openbase.bco.stage.registry.JavaFX3dObjectRegistrySynchronizer;
import org.openbase.bco.stage.registry.ObjectBoxFactory;
import org.openbase.bco.stage.registry.RegistryRoomFactory;
import org.openbase.bco.stage.rsb.RSBConnection;
import org.openbase.bco.stage.visualization.GUIManager;
import org.openbase.bco.stage.visualization.ObjectBox;
import org.openbase.bco.stage.visualization.RegistryRoom;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import org.openbase.type.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;

/**
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public final class StageController extends AbstractEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageController.class);
    private static StageController instance;

    private JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder> objectBoxRegistrySynchronizer;
    private JavaFX3dObjectRegistrySynchronizer<String, RegistryRoom, UnitConfig, UnitConfig.Builder> roomRegistrySynchronizer;

    private List<String> registryFlags;
    private boolean usePSCFilter;
    private boolean connectedRegistry = false;

    private RSBConnection rsbConnection;

    private StageController(Stage primaryStage) {
        try {
            try {
                GUIManager.initInstance(primaryStage);
            } catch (InstantiationException ex) {
                throw new CouldNotPerformException("Could not initialize GUIManager.", ex);
            }

            try {
                usePSCFilter = JPService.getProperty(JPFilterPscUnits.class).getValue();
                LOGGER.info("Filter for psc units set to: " + usePSCFilter);
                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();
                LOGGER.info("Filter for psc units: " + registryFlags);

                if (!JPService.getProperty(JPDisableRegistry.class).getValue()) {
                    initializeRegistryConnection();
                }

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
            } catch (CouldNotPerformException | JPNotAvailableException | InterruptedException ex) {
                objectBoxRegistrySynchronizer.deactivate();
                GUIManager.getInstance().close();
                throw ex;
            }
        } catch (Exception ex) {
            criticalError(ex);
        }
    }

    // TODO list:
    // -InterruptedException niemals fangen!!!
    // -Check behavior of RegistrySynchronizer in case an unverified object becomes verified. (Should register but maybe only update called).
    public synchronized static StageController initInstance(Stage primaryStage) {
        if (instance == null) {
            instance = new StageController(primaryStage);
        }
        return instance;
    }

    public synchronized static StageController getInstance() {
        return instance;
    }

    public static final void criticalError(Exception ex) {
        ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
        Platform.exit();
        System.exit(255);
    }

    @Override
    public void handleEvent(final Event event) {
        if (event.getData() instanceof TrackedPostures3DFloat) {
            LOGGER.trace("New TrackedPostures3DFloat event received.");
            final TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
            GUIManager.getInstance().updateSkeletonData(postures);
        } else if (event.getData() instanceof PointingRay3DFloatDistributionCollection) {
            LOGGER.trace("New PointingRay3DFloatCollection event received.");
            final PointingRay3DFloatDistributionCollection pointingRays = (PointingRay3DFloatDistributionCollection) event.getData();
            GUIManager.getInstance().updateRayData(pointingRays);
        } else if (event.getData() instanceof UnitProbabilityCollection) {
            LOGGER.trace("New UnitProbabilityCollection event received.");
            final UnitProbabilityCollection selectedUnits = (UnitProbabilityCollection) event.getData();
            GUIManager.getInstance().highlightObjects(selectedUnits);
        }
    }

    public void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        if (connectedRegistry) {
            return;
        }
        try {
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.getUnitRegistry().waitForData();

            this.objectBoxRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<>(GUIManager.getInstance().getObjectGroup(),
                    GUIManager.getInstance().getObjectBoxRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(true), getUnitRegistry(), ObjectBoxFactory.getInstance());
            this.objectBoxRegistrySynchronizer.addFilter(config -> {
                try {
                    if (usePSCFilter) {
                        return !PointingUnitChecker.isPointingControlUnit(config, registryFlags);
                    } else {
                        return !PointingUnitChecker.isDalOrGroupWithLocation(config);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ExceptionPrinter.printHistory(new VerificationFailedException("Verification of config " + config.getLabel() + " failed.", ex), LOGGER);
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new VerificationFailedException("Verification of config " + config.getLabel() + " failed.", ex), LOGGER);
                }
                // filter in case an exception occurs.
                return true;
            });

            this.roomRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<>(GUIManager.getInstance().getRoomGroup(),
                    GUIManager.getInstance().getRoomRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(true), getUnitRegistry(), RegistryRoomFactory.getInstance());
            this.roomRegistrySynchronizer.addFilter(config -> {
                try {
                    return config.getUnitType() != UnitType.LOCATION || !PointingUnitChecker.hasLocationData(config);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ExceptionPrinter.printHistory(new VerificationFailedException("Verification of config " + config.getLabel() + " failed.", ex), LOGGER);
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new VerificationFailedException("Verification of config " + config.getLabel() + " failed.", ex), LOGGER);
                }
                // filter in case an exception occurs.
                return true;
            });

            Registries.waitForData();
            objectBoxRegistrySynchronizer.activate();
            roomRegistrySynchronizer.activate();
            connectedRegistry = true;
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not connect to the registry. To try reconnecting, hit C.", ex), LOGGER, LogLevel.WARN);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }
}
