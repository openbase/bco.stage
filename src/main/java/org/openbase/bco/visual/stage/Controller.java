/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.visual.stage;

/*-
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

import org.openbase.bco.visual.stage.visualization.GUIManager;
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
