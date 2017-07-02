/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.stage.visualization;

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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rst.tracking.PointingRay3DFloatCollectionType;
import rst.tracking.TrackedPosture3DFloatType;
import rst.tracking.TrackedPostures3DFloatType;

/**
 *
 * @author thoren
 */
public class GUIManager {
    // TODO 
    // -Fix bugs: body parts and rays floating around + Exception in thread "JavaFX Application Thread" java.lang.ArrayIndexOutOfBoundsException
    // -Visualize objects
    private static final double AXIS_LENGTH = 250.0;
    private static final double AXIS_WIDTH = 0.03;
    private final Skeleton[] skeletons = new Skeleton[6];
    private final List<Ray> rays = new ArrayList<>();

    private final Group root = new Group();
    private final Group axisGroup = new Group();
    private final Group world = new Group();
    private final MoveableCamera camera;
    
    private final MaterialManager mm = new MaterialManager();
    
    private void connectCamera(Scene scene){
        scene.setOnMousePressed(camera);
        scene.setOnMouseDragged(camera);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                switch(event.getCode()){
                    case X:
                        axisGroup.setVisible(!axisGroup.isVisible());
                        break;
                    default:
                        camera.handle(event);
                        break;
                }
            }
        });
        scene.setOnKeyReleased(camera);
    }

    private void buildAxes() {
        System.out.println("buildAxes()");
        
        final Line xLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.red, new Point3D(-AXIS_LENGTH,0,0), new Point3D(AXIS_LENGTH,0,0));
        final Line yLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.green, new Point3D(0,-AXIS_LENGTH,0), new Point3D(0,AXIS_LENGTH,0));
        final Line zLine = new Line(Line.LineType.BOX, AXIS_WIDTH, mm.blue, new Point3D(0,0,-AXIS_LENGTH), new Point3D(0,0,AXIS_LENGTH));
        
        axisGroup.getChildren().addAll(xLine, yLine, zLine);
        axisGroup.setVisible(true);
        world.getChildren().add(axisGroup);
    }
    
    private void buildSkeletons() {
        for (int i = 0; i < skeletons.length; i++){
            skeletons[i] = new Skeleton();
            world.getChildren().add(skeletons[i]);
            skeletons[i].setVisible(false);
        }
    }
    
    public GUIManager(Stage primaryStage){
        camera = new MoveableCamera();

        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);

//        buildScene();
        buildAxes();
        world.getChildren().add(new Room());
        buildSkeletons();

        Scene scene = new Scene(root, 1024, 768, true);
        connectCamera(scene);
        scene.setFill(Color.GREY);

        primaryStage.setTitle("Skeleton Animator");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setCamera(camera);
        
    }
    
    public void updateOrCreateSkeletons(TrackedPostures3DFloatType.TrackedPostures3DFloat postures){
        for(int i = 0; i < postures.getPostureCount(); i++){
            TrackedPosture3DFloatType.TrackedPosture3DFloat posture = postures.getPosture(i);
            if(posture.getConfidenceCount() > 0){
                skeletons[i].setVisible(true);
                skeletons[i].updatePositions(posture);
            } else {
                skeletons[i].setVisible(false);
            }
        }
    }
    
    public void updateOrCreateRays(PointingRay3DFloatCollectionType.PointingRay3DFloatCollection pointingRays){
        System.out.println("updateRays");
        int difference = pointingRays.getElementCount() - rays.size();
        if(difference > 0){
            for(int i = 0; i < difference; i++){
                Ray r = new Ray();
                rays.add(r);
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        world.getChildren().add(r);
                    }
                });
            }
        } else {
            for(int i = 0; i < -difference; i++){
                Ray r = rays.get(pointingRays.getElementCount());
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        world.getChildren().remove(r);
                    }
                });
                rays.remove(pointingRays.getElementCount());
            }
        }
        for(int i = 0; i < pointingRays.getElementCount(); i++){
            rays.get(i).update(pointingRays.getElement(i));
        }
    }
}
