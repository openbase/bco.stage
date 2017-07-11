/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.stage.visualization;

import java.util.HashMap;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import org.openbase.bco.stage.registry.BoundingBox;
import rct.Transform;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ObjectGroup extends Group {
    private final AnimationTimer objectLoop;
    private final Map<String, ObjectBox> objects = new HashMap<>();
    private final Map<String, BoundingBox> newObjects = new HashMap<>();
    
    
    public ObjectGroup(){
        
        
        objectLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
            }
        };
        objectLoop.start();
    }
    
    public synchronized void add(UnitConfigType.UnitConfig config, Transform toRootCoordinateTransform){
        newObjects.put(config.getId(), new BoundingBox(toRootCoordinateTransform.getTransform(), config.getPlacementConfig().getShape().getBoundingBox()));
        
    }
}
