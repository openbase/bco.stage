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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.openbase.bco.psc.lib.registry.SynchronizableRegistryImpl;
import org.openbase.bco.stage.Controller;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.storage.registry.SynchronizableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public final class GUIManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GUIManager.class);
    
    private static final double AXIS_LENGTH = 250.0;
    private static final double AXIS_WIDTH = 0.03;
    private final Skeleton[] skeletons = new Skeleton[6];
    private final List<PointingRay> rays = new ArrayList<>();
    private final SynchronizableRegistryImpl<String, ObjectBox> objectBoxRegistry;

    private final Stage primaryStage;
    private final Group root = new Group();
    private final Group world = new Group();
    private final Group axisGroup = new Group();
    private final Group skeletonGroup = new Group();
    private final Group rayGroup = new Group();
    private final Group objectGroup = new Group();
    private final MoveableCamera camera;
    private final Room room;
    
    private final Controller controller;
    
    public GUIManager(Stage primaryStage, Controller ctrl) throws InstantiationException{
        this.controller = ctrl;
        try {
            LOGGER.info("Setting up the 3D - scene.");
            this.primaryStage = primaryStage;
            objectBoxRegistry = new SynchronizableRegistryImpl<>();
            
            camera = new MoveableCamera();
            
            root.getChildren().add(world);
            root.setDepthTest(DepthTest.ENABLE);
            
            buildAxes();
            room = new Room();
            world.getChildren().add(room);
            world.getChildren().add(objectGroup);
            world.getChildren().add(rayGroup);
            buildSkeletons();
            
            Scene scene = new Scene(root, 1024, 768, true);
            connectCamera(scene);
            scene.setFill(Color.GREY);
            
            primaryStage.setTitle(JPService.getApplicationName());
            primaryStage.setScene(scene);
            primaryStage.show();
            
            primaryStage.setOnCloseRequest(e->{
                LOGGER.info("Close called on primary stage.");
                Platform.exit();
                System.exit(0);
            });
            
            scene.setCamera(camera);
        } catch (InstantiationException ex) {
            throw new InstantiationException(this, ex);
        }
    }
    
    public void close(){
        LOGGER.info("close called on GUIManager");
//        primaryStage.fireEvent(
//            new WindowEvent(
//                primaryStage,
//                WindowEvent.WINDOW_CLOSE_REQUEST
//            )
//        );
        primaryStage.close();
    }
    
    public synchronized void updateSkeletonData(TrackedPostures3DFloat postures){
        updateOrCreateSkeletons(postures);
    }
    
    public synchronized void updateRayData(PointingRay3DFloatCollection pointingRays){
        updateOrCreateRays(pointingRays);
    }
    
    private void connectCamera(Scene scene){
        LOGGER.debug("Connecting camera to the scene.");
        scene.setOnMousePressed(camera);
        scene.setOnMouseDragged(camera);
        scene.setOnKeyPressed(event -> {
            switch(event.getCode()){
                case X:
                    axisGroup.setVisible(!axisGroup.isVisible());
                    break;
                case R:
                    rayGroup.setVisible(!rayGroup.isVisible());
                    break;
                case P:
                    skeletonGroup.setVisible(!skeletonGroup.isVisible());
                    break;
                case M:
                    room.setVisible(!room.isVisible());
                    break;
                case O:
                    objectGroup.setVisible(!objectGroup.isVisible());
                    break;
                case H:
                    //TODO: show help here.
                    break;
                case C:
                    try {
                        controller.initializeRegistryConnection();
                    } catch (InterruptedException | CouldNotPerformException ex) {
                        Controller.criticalError(ex);
                    }
                    break;
                default:
                    camera.handle(event);
                    break;
            }
        });
        scene.setOnKeyReleased(camera);
    }

    private void buildAxes() {
        LOGGER.debug("Creating axes visualization.");
        
        PhongMaterialManager mm = PhongMaterialManager.getInstance();
        final Line xLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.red, new Point3D(-AXIS_LENGTH,0,0), new Point3D(AXIS_LENGTH,0,0));
        final Line yLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.green, new Point3D(0,-AXIS_LENGTH,0), new Point3D(0,AXIS_LENGTH,0));
        final Line zLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.blue, new Point3D(0,0,-AXIS_LENGTH), new Point3D(0,0,AXIS_LENGTH));
        
        axisGroup.getChildren().addAll(xLine, yLine, zLine);
        axisGroup.setVisible(true);
        world.getChildren().add(axisGroup);
    }
    
    private void buildSkeletons() {
        LOGGER.debug("Creating skeleton visualizations.");
        
        for (int i = 0; i < skeletons.length; i++){
            skeletons[i] = new Skeleton();
            skeletonGroup.getChildren().add(skeletons[i]);
            skeletons[i].setVisible(false);
        }
        world.getChildren().add(skeletonGroup);
    }
    
    private synchronized void updateOrCreateSkeletons(TrackedPostures3DFloat postures){
        Platform.runLater(() -> {
            for(int i = 0; i < postures.getPostureCount(); i++){
                TrackedPosture3DFloat posture = postures.getPosture(i);
                if(posture.getConfidenceCount() > 0){
                    skeletons[i].updatePositions(posture);
                    skeletons[i].setVisible(true);
                } else {
                    skeletons[i].setVisible(false);
                }
            }
        });
    }
    
    private synchronized void updateOrCreateRays(PointingRay3DFloatCollection pointingRays){
        Platform.runLater(() -> {
            LOGGER.trace("Updating or creating rays.");
            int difference = pointingRays.getElementCount() - rays.size();
            if(difference > 0){
                LOGGER.trace("Adding new rays.");
                for(int i = 0; i < difference; i++){
                    PointingRay r = new PointingRay();
                    rays.add(r);
                    rayGroup.getChildren().add(r);
                }
            } else {
                LOGGER.trace("Removing rays.");
                for(int i = 0; i < -difference; i++){
                    PointingRay r = rays.get(pointingRays.getElementCount());
                    rayGroup.getChildren().remove(r);
                    rays.remove(pointingRays.getElementCount());
                }
            }
            LOGGER.trace("Updating existing rays.");
            for(int i = 0; i < pointingRays.getElementCount(); i++){
                rays.get(i).update(pointingRays.getElement(i));
            }
        });
    }
    
    public Group getObjectGroup(){
        return objectGroup;
    }

    public SynchronizableRegistry<String, ObjectBox> getObjectBoxRegistry() {
        return objectBoxRegistry;
    }

    public void highlightObject(String id) {
        try {
            if(objectBoxRegistry.contains(id)){
                objectBoxRegistry.get(id).highlight();
            } else {
                LOGGER.trace("ObjectBox with id " + id + " not found in local registry.");
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not check objectBoxRegistry for id: " + id, ex), LOGGER, LogLevel.WARN);
        }
    }
}
