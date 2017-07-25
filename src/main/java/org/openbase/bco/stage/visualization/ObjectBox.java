package org.openbase.bco.stage.visualization;

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

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.stage.Controller;
import org.openbase.bco.stage.registry.JavaFX3dObjectRegistryEntry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.geometry.AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ObjectBox implements JavaFX3dObjectRegistryEntry<String, UnitConfig>{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ObjectBox.class);
    
    private static final Material DEFAULT_MATERIAL = PhongMaterialManager.getInstance().white;
    private static final Material HIGHTLIGHT_MATERIAL = PhongMaterialManager.getInstance().green;
    private static final int COOLDOWN_TIME = 3000;
    
    private final Box box;
    
    private UnitConfig config;
    private Thread cooldownThread;
    private long highlightEndTime;

    public ObjectBox() {
        box = new Box();
        box.setVisible(false);
        box.setMaterial(DEFAULT_MATERIAL);
    }

    @Override
    public synchronized UnitConfig applyConfigUpdate(UnitConfig config) throws InterruptedException, CouldNotPerformException {
        try {
            this.config = config;
            AxisAlignedBoundingBox3DFloat boundingBox = config.getPlacementConfig().getShape().getBoundingBox();
            AbstractUnitRemote unit = (AbstractUnitRemote) Units.getUnit(config, false);
            Point3d center = unit.getGlobalBoundingBoxCenterPoint3d();
            AxisAngle4d aa =new AxisAngle4d();
            aa.set(unit.getGlobalRotationQuat4d());
            
            Platform.runLater(() -> {
                box.setVisible(true);
                
                box.setTranslateX(center.x);
                box.setTranslateY(center.y);
                box.setTranslateZ(center.z);
                
                box.setWidth(boundingBox.getWidth());
                box.setDepth(boundingBox.getDepth());
                box.setHeight(boundingBox.getHeight());
                
                box.setRotationAxis(new Point3D(aa.x, aa.y, aa.z));
                box.setRotate(aa.angle/Math.PI*180);
            });
            return this.config;
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("applyConfigUpdate failed.", ex);
        }
    }

    @Override
    public synchronized UnitConfig getConfig() throws NotAvailableException {
        if(config == null) throw new NotAvailableException("Config");
        return config;
    }
     
    @Override
    public synchronized String getId() throws NotAvailableException{
        if(config == null) throw new NotAvailableException("Id");
        return config.getId();
    }
    
    @Override
    public Box getNode(){
        return box;
    }
    
    public synchronized void highlight(){
        setMaterial(HIGHTLIGHT_MATERIAL);
        highlightEndTime = System.currentTimeMillis()+COOLDOWN_TIME;
        LOGGER.trace("highlighting ObjectBox of " + config.getLabel() + " with id: " + config.getId());
        if(cooldownThread == null || !cooldownThread.isAlive()){
            cooldownThread = new Thread(() -> {
                long time_diff = getHighlightEndTime() - System.currentTimeMillis();
                while(time_diff > 0){
                    try {
                        Thread.sleep(time_diff);
                    } catch (InterruptedException ex) {
                        Controller.criticalError(ex);
                    }
                    time_diff = getHighlightEndTime() - System.currentTimeMillis();
                }
                setMaterial(DEFAULT_MATERIAL);
            });
        }
        cooldownThread.start();
    }
    
    private synchronized long getHighlightEndTime(){
        return highlightEndTime;
    }
    
    private void setMaterial(Material material){
        Platform.runLater(() -> 
                box.setMaterial(material)
        );
    }
}
