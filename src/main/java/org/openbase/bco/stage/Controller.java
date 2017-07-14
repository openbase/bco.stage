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
import org.openbase.bco.stage.visualization.GUIManager;
import javafx.stage.Stage;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
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
import org.openbase.bco.stage.registry.SynchronizableRegistryImpl;
import org.openbase.bco.stage.visualization.ObjectBox;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.VerificationFailedException;
import rst.configuration.EntryType;
import rst.configuration.MetaConfigType;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Controller extends AbstractEventHandler{
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private final GUIManager guiManager;
    private RSBConnection rsbConnection;
    private ObjectBoxFactory factory;
    private SynchronizableRegistryImpl<String, ObjectBox> objectBoxRegistry;
    private JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder> appRegistrySynchronizer;
    
    private List<String> REGISTRY_FLAGS;
    
    public Controller(Stage primaryStage){
        guiManager = new GUIManager(primaryStage);
        this.factory = ObjectBoxFactory.getInstance();

        try {
//            if(Registries.isDataAvailable()){
            REGISTRY_FLAGS = JPService.getProperty(JPRegistryFlags.class).getValue();
            this.objectBoxRegistry = new SynchronizableRegistryImpl<>();
            this.appRegistrySynchronizer = new JavaFX3dObjectRegistrySynchronizer<String, ObjectBox, UnitConfig, UnitConfig.Builder>(guiManager.getObjectGroup(), objectBoxRegistry, getUnitRegistry().getUnitConfigRemoteRegistry(), factory) {
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
            appRegistrySynchronizer.activate();
            
            rsbConnection = new RSBConnection(this);
        } catch (Exception ex) {
            guiManager.close();
            ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
            System.exit(255);
        }
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
            System.out.println("Location data available for " +config.getLabel());
            return true;
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER);
            System.err.println("No location data available for " +config.getLabel());
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
            if (REGISTRY_FLAGS.contains(entry.getKey()))
                return true;
        }
        return false;
    }
}
