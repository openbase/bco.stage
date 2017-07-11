/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.stage.registry;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openbase.bco.dal.remote.unit.Units;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import static org.openbase.bco.registry.remote.Registries.getLocationRegistry;
import org.openbase.bco.stage.jp.JPRegistryFlags;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.extension.rct.GlobalTransformReceiver;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rct.Transform;
import rct.TransformerException;
import rst.configuration.EntryType.Entry;
import rst.configuration.MetaConfigType.MetaConfig;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryManager implements Observer<UnitRegistryData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryManager.class);
    private RegistryObjectInterface objectManager;
    private final List<String> REGISTRY_FLAGS;
    private ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> dalUnitDiffer;
    private ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> groupDiffer;

    public RegistryManager(RegistryObjectInterface objectManager) throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Initializing Registry Manager.");
        this.objectManager = objectManager;
        try {
            REGISTRY_FLAGS = JPService.getProperty(JPRegistryFlags.class).getValue();
            
            dalUnitDiffer = new ProtobufListDiff<>();
            groupDiffer = new ProtobufListDiff<>();
            
            LOGGER.info("Waiting for UnitRegistry");
            getUnitRegistry().waitForData(3000, TimeUnit.MILLISECONDS);
            LOGGER.info("Waiting for LocationRegistry");
            getLocationRegistry().waitForData(100, TimeUnit.MILLISECONDS);
            
            getUnitRegistry().addDataObserver(this);
            
            updateSelectables();
            LOGGER.info("Registry Manager successfully initialized.");
        } catch (JPNotAvailableException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not initialize connection!", ex);
        }
    }
    
    private synchronized void updateSelectables() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("updateSelectables() called.");
        try {
            dalUnitDiffer.diff(getUnitRegistry().getDalUnitConfigs());
            updateUnits(dalUnitDiffer, false);
            groupDiffer.diff(getUnitRegistry().getUnitGroupConfigs());
            updateUnits(groupDiffer, true);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update selectables", ex);
        }
    }
    
    private void updateUnits(ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> unitDiffer, boolean isGroup) throws CouldNotPerformException, InterruptedException {
        // TODO Hier nochmal alles überarbeiten!!!
        if(isGroup) LOGGER.info("updatUnits() for group.");
        else LOGGER.info("updatUnits() for DAL-units.");
        try{
            LOGGER.info("updateUnits, originMessages: " + unitDiffer.getOriginMessages().size());
            LOGGER.info("updateUnits, removedMessages: " + unitDiffer.getRemovedMessageMap().getMessages().size());
            LOGGER.info("updateUnits, newMessages: " + unitDiffer.getNewMessageMap().getMessages().size());
            LOGGER.info("updateUnits, updatedMessages: " + unitDiffer.getUpdatedMessageMap().getMessages().size());
            for(UnitConfig config : unitDiffer.getRemovedMessageMap().getMessages()){
                objectManager.removeById(config.getId());
            }
            for(UnitConfig config : unitDiffer.getUpdatedMessageMap().getMessages()){
                if(isApplicableUnit(config)){
                    if(isGroup) LOGGER.info("updateUnits inside UpdatedMessages.");
                    String rootTransform = getLocationRegistry().getRootLocationConfig().getPlacementConfig().getTransformationFrameId();
                    // Lookup the transform
                    Transform toRootCoordinateTransform = GlobalTransformReceiver.getInstance().lookupTransform(config.getPlacementConfig().getTransformationFrameId(), 
                            rootTransform,
                            System.currentTimeMillis());
                    objectManager.update(config.getId(), config, toRootCoordinateTransform);
                } else {
                    objectManager.removeById(config.getId());
                }
            }
            for(UnitConfig config : unitDiffer.getNewMessageMap().getMessages()){
                if(isGroup && config.getLabel().equals("LLamp2")){
//                if(isApplicableUnit(config)){
                    if (EnablingStateType.EnablingState.State.ENABLED == config.getEnablingState().getValue() && config.getPlacementConfig().hasPosition()){
                        LOGGER.info("updateUnits inside newMessages.");
                        String rootTransform = getLocationRegistry().getRootLocationConfig().getPlacementConfig().getTransformationFrameId();

                        LOGGER.info("updateUnits after rootTransform: " + rootTransform);

                        try {
                            // Lookup the transform
                            Transform toRootCoordinateTransform = Units.getUnitTransformation(getLocationRegistry().getRootLocationConfig(), config).get(1000, TimeUnit.MILLISECONDS);
                            
                            // TODO Ask Marian about missing TransformationFrameId!!
                            
//                            Transform toRootCoordinateTransform = GlobalTransformReceiver.getInstance().lookupTransform(config.getPlacementConfig().getTransformationFrameId(), 
//                                    rootTransform,
//                                    System.currentTimeMillis());
                            objectManager.add(config, toRootCoordinateTransform);
                        } catch (TimeoutException ex) {
                            if (isGroup) ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load transformation for unit-group " + config.getLabel(), ex), LOGGER);
                            else ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load transformation for dal-unit " + config.getLabel(), ex), LOGGER);
                        }
                    } else {
                        if (isGroup) LOGGER.warn("Can not load transformation for unit-group " + config.getLabel() + ", please make sure, it is enabled and its position is set.");
                        else LOGGER.warn("Can not load transformation for dal-unit " + config.getLabel() + ", please make sure, it is enabled and its position is set.");
                    }
                }
            }
//        } catch (TransformerException | CouldNotPerformException | InterruptedException ex) {
        } catch (TransformerException | CouldNotPerformException | ExecutionException ex) {
            if(isGroup)
                throw new CouldNotPerformException("Could not update group units", ex);
            else 
                throw new CouldNotPerformException("Could not update dal units", ex);
        }
    }
    
    private boolean isRegistryFlagSet(MetaConfig meta){
        if(meta == null || meta.getEntryList() == null) 
            return false;
        for (Entry entry : meta.getEntryList()) {
            if (REGISTRY_FLAGS.contains(entry.getKey()))
                return true;
        }
        return false;
    }
    
    private boolean isApplicableUnit(UnitConfig config) throws InterruptedException {
        if (config != null && isRegistryFlagSet(config.getMetaConfig())) {
            for (ServiceConfig sc : config.getServiceConfigList()) {
                ServiceTemplate.ServiceType type;
                try {
                    type = getUnitRegistry().getServiceTemplateById(sc.getServiceDescription().getServiceTemplateId()).getType();
                } catch (CouldNotPerformException ex) {
                    type = sc.getServiceDescription().getType();
                }
                if (ServiceTemplate.ServiceType.POWER_STATE_SERVICE == type
                        && ServiceTemplate.ServicePattern.OPERATION == sc.getServiceDescription().getPattern()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void update(Observable<UnitRegistryData> source, UnitRegistryData data) throws Exception {
        LOGGER.info("Updated data for DAL-units or Unit-groups.");
        updateSelectables();
    }
}
