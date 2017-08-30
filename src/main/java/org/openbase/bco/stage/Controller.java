package org.openbase.bco.stage;

/*-
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
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
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public final class Controller extends AbstractEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private static Controller instance;

    private JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder> objectBoxRegistrySynchronizer;
    private JavaFX3dObjectRegistrySynchronizer<String, RegistryRoom, UnitConfig, UnitConfig.Builder> roomRegistrySynchronizer;

    private List<String> registryFlags;
    private boolean usePSCFilter;
    private boolean connectedRegistry = false;

    // TODO list:
    // -InterruptedException niemals fangen!!!
    // -JavaFx stuff wie Line oder Ray in jul.visual.javafx einpflegen
    // - Remove mainLoop and replace by runLater stuff in the components.
    // -Check behavior of RegistrySynchronizer in case an unverified object becomes verified. (Should register but maybe only update called).
    public synchronized static Controller initInstance(Stage primaryStage) {
        if (instance == null) {
            instance = new Controller(primaryStage);
        }
        return instance;
    }

    public synchronized static Controller getInstance() {
        return instance;
    }

    private Controller(Stage primaryStage) {
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

                RSBConnection.initialize(this);
            } catch (CouldNotPerformException | JPNotAvailableException | InterruptedException ex) {
                objectBoxRegistrySynchronizer.deactivate();
                GUIManager.getInstance().close();
                throw ex;
            }
        } catch (Exception ex) {
            criticalError(ex);
        }
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
            TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
            GUIManager.getInstance().updateSkeletonData(postures);
        } else if (event.getData() instanceof PointingRay3DFloatDistributionCollection) {
            LOGGER.trace("New PointingRay3DFloatCollection event received.");
            PointingRay3DFloatDistributionCollection pointingRays = (PointingRay3DFloatDistributionCollection) event.getData();
            GUIManager.getInstance().updateRayData(pointingRays);
        } else if (event.getData() instanceof UnitProbabilityCollection) {
            LOGGER.trace("New UnitProbabilityCollection event received.");
            UnitProbabilityCollection selectedUnits = (UnitProbabilityCollection) event.getData();
            GUIManager.getInstance().highlightObjects(selectedUnits);
        }
    }

    public void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        if (connectedRegistry) {
            return;
        }
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);

            this.objectBoxRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder>(GUIManager.getInstance().getObjectGroup(),
                    GUIManager.getInstance().getObjectBoxRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), ObjectBoxFactory.getInstance()) {
                @Override
                public boolean verifyConfig(UnitConfig config) throws VerificationFailedException {
                    try {
                        if (usePSCFilter) {
                            return PointingUnitChecker.isPointingControlUnit(config, registryFlags);
                        } else {
                            return PointingUnitChecker.isDalOrGroupWithLocation(config);
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
                        return false;
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
                        return false;
                    }
                }
            };

            this.roomRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<String, RegistryRoom, UnitConfig, UnitConfig.Builder>(GUIManager.getInstance().getRoomGroup(),
                    GUIManager.getInstance().getRoomRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), RegistryRoomFactory.getInstance()) {
                @Override
                public boolean verifyConfig(UnitConfig config) throws VerificationFailedException {
                    return config.getType() == UnitType.LOCATION;
                }
            };

            Registries.waitForData();
            objectBoxRegistrySynchronizer.activate();
            roomRegistrySynchronizer.activate();
            connectedRegistry = true;
        } catch (NotAvailableException ex) {
            //TODO: Add here what to press.
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not connect to the registry. To try reconnecting, hit C.", ex), LOGGER, LogLevel.WARN);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }
}
