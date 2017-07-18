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
import java.util.logging.Level;
import javafx.application.Platform;
import org.openbase.bco.stage.visualization.GUIManager;
import javafx.stage.Stage;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.bco.stage.jp.JPDisableRegistry;
import org.openbase.bco.stage.jp.JPRegistryFlags;
import org.openbase.bco.stage.registry.ObjectBoxFactory;
import org.openbase.bco.stage.registry.JavaFX3dObjectRegistrySynchronizer;
import org.openbase.bco.stage.rsb.RSBConnection;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;
import org.openbase.bco.stage.visualization.ObjectBox;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.LogLevel;
import rst.configuration.EntryType;
import rst.configuration.MetaConfigType;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public final class Controller extends AbstractEventHandler{
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private GUIManager guiManager;
    private RSBConnection rsbConnection;
    private JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder> objectBoxRegistrySynchronizer;
    
    private List<String> registryFlags;
    private boolean connectedRegistry = false;
    
    // TODO list:
    // -InterruptedException niemals fangen!!!
    // -GetPosition und GetGlobalPosition für UnitRemotes implementieren und PullRequest stellen. In AbstractUnitRemote -- PullRequest muss durchgehen.
    // -Auf Bearbeitung von Ticket #52 warten, bzw code entsprechend anpasssen.
    // -JavaFx stuff wie Line oder Ray in jul.visual.javafx einpflegen
    // - Remove mainLoop and replace by runLater stuff in the components.
    
    public Controller(Stage primaryStage){
        try{
            try{
                guiManager = new GUIManager(primaryStage, this);
            } catch (InstantiationException ex) {
                throw new CouldNotPerformException("Could not initialize GUIManager.", ex);
            }

            try {
                registryFlags = JPService.getProperty(JPRegistryFlags.class).getValue();
                
                if(!JPService.getProperty(JPDisableRegistry.class).getValue()){
                    initializeRegistryConnection();
                }

                rsbConnection = new RSBConnection(this);
            } catch (CouldNotPerformException | JPNotAvailableException | InterruptedException ex) {
                guiManager.close();
                objectBoxRegistrySynchronizer.deactivate();
                throw ex;
            }
        } catch(Exception ex){
            criticalError(ex);
        }
    }
    
    public static final void criticalError(Exception ex){
        ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
        Platform.exit();
        System.exit(255);
    }
    
    @Override
    public void handleEvent(final Event event) {
        if(event.getData() instanceof TrackedPostures3DFloat){
            LOGGER.trace("New TrackedPostures3DFloat event received.");
            TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
            guiManager.updateSkeletonData(postures);
        } else if(event.getData() instanceof PointingRay3DFloatCollection){
            LOGGER.trace("New PointingRay3DFloatCollection event received.");
            PointingRay3DFloatCollection pointingRays = (PointingRay3DFloatCollection) event.getData();
            guiManager.updateRayData(pointingRays);
        } else if(event.getData() instanceof UnitProbabilityCollection) {
            LOGGER.trace("New UnitProbabilityCollection event received.");
            UnitProbabilityCollection selectedUnits = (UnitProbabilityCollection) event.getData();
            //TODO process unit/units correctly
            guiManager.highlightObject(selectedUnits.getElement(0).getId());
        }
    }
    
    public void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException{
        if(connectedRegistry) return;
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);
            
            this.objectBoxRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder>(guiManager.getObjectGroup(), 
                    guiManager.getObjectBoxRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), ObjectBoxFactory.getInstance()) {
                @Override
                public boolean verifyConfig(UnitConfig config) throws VerificationFailedException {
                    try {
                        return isApplicableUnit(config);
                    } catch (InterruptedException ex) {
                        ExceptionPrinter.printHistory(ex, logger);
                        return false;
                    }
                }
            };
            
            Registries.waitForData(); 
            objectBoxRegistrySynchronizer.activate();
            connectedRegistry = true;
        } catch (NotAvailableException ex) {
            //TODO: Add here what to press.
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not connect to the registry. To try reconnecting, hit C.", ex), LOGGER, LogLevel.WARN);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }
    
    private boolean isApplicableUnit(UnitConfig config) throws InterruptedException {
        if (config != null && isRegistryFlagSet(config.getMetaConfig())) {
            return hasPowerStateService(config) && hasLocationData(config);
        }
        return false;
    }
    
    private boolean hasLocationData(UnitConfig config) throws InterruptedException{
        try {
            AbstractUnitRemote unitRemote = (AbstractUnitRemote) Units.getUnit(config, false);
            unitRemote.getGlobalBoundingBoxCenterPoint3d();
            return true;
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
            return false;
        }
    }
    
    private boolean hasPowerStateService(UnitConfig config) throws InterruptedException{
        for (ServiceConfigType.ServiceConfig sc : config.getServiceConfigList()) {
            ServiceTemplateType.ServiceTemplate.ServiceType type;
            try {
                type = getUnitRegistry().getServiceTemplateById(sc.getServiceDescription().getServiceTemplateId()).getType();
            } catch (CouldNotPerformException ex) {
                type = sc.getServiceDescription().getType();
            } 
            if (ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE == type
                    && ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION == sc.getServiceDescription().getPattern()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isRegistryFlagSet(MetaConfigType.MetaConfig meta){
        if(meta == null || meta.getEntryList() == null) 
            return false;
        for (EntryType.Entry entry : meta.getEntryList()) {
            if (registryFlags.contains(entry.getKey()))
                return true;
        }
        return false;
    }
}
