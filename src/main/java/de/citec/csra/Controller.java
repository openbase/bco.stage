/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra;

import de.citec.csra.visualization.GUIManager;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author thoren
 */
public class Controller extends AbstractEventHandler{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private final GUIManager guiManager;
    
    public Controller(Stage primaryStage){
        guiManager = new GUIManager(primaryStage);
    }
    
    @Override
    public void handleEvent(final Event event) {
        if(event.getData() instanceof TrackedPostures3DFloat){
            TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
            guiManager.updateOrCreateSkeletons(postures);
        } else if(event.getData() instanceof PointingRay3DFloatCollection){
            PointingRay3DFloatCollection pointingRays = (PointingRay3DFloatCollection) event.getData();
            guiManager.updateOrCreateRays(pointingRays);
        }
    }
}
