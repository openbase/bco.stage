/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.visual.stage.visualization;

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

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 *
 * @author thoren
 */
public class MoveableCamera extends PerspectiveCamera implements EventHandler{
    private static final double CAMERA_INITIAL_X = -7.0;
    private static final double CAMERA_INITIAL_Y = 8.0;
    private static final double CAMERA_INITIAL_Z = 3.1;
    private static final double CAMERA_INITIAL_Y_ANGLE = 100.0;
    private static final double CAMERA_INITIAL_X_ANGLE = -15.0;
    private static final double MOVEMENT_FACTOR_HOR = 0.1;
    private static final double MOVEMENT_FACTOR_VERT = 0.02;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final Rotate PRE_ROTATE = new Rotate(-90, Rotate.X_AXIS);
    
    private KeyState keyState;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    
    private final Translate cameraTranslate = new Translate(CAMERA_INITIAL_X, CAMERA_INITIAL_Y, CAMERA_INITIAL_Z);
    private final Rotate cameraRotateX = new Rotate(CAMERA_INITIAL_X_ANGLE, Rotate.X_AXIS);
    private final Rotate cameraRotateY = new Rotate(CAMERA_INITIAL_Y_ANGLE, Rotate.Y_AXIS);
    
    public MoveableCamera(){
        super(true);
        this.keyState = new KeyState();
        
        this.setNearClip(CAMERA_NEAR_CLIP);
        this.setFarClip(CAMERA_FAR_CLIP);
        this.getTransforms().addAll(cameraTranslate, PRE_ROTATE, cameraRotateY, cameraRotateX);
        
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Rotate rotation = new Rotate(-cameraRotateY.getAngle(), Rotate.Z_AXIS);
                Point3D forward = rotation.transform(new Point3D(0, 1, 0));
                Point3D right = rotation.transform(new Point3D(1, 0, 0));
                double sMovement = 0.0;
                if(keyState.isaPressed()) sMovement -= 1.0;
                if(keyState.isdPressed()) sMovement += 1.0;
                double fMovement = 0.0;
                if(keyState.issPressed()) fMovement -= 1.0;
                if(keyState.iswPressed()) fMovement += 1.0;
                Point3D movement = forward.multiply(fMovement).add(right.multiply(sMovement)).multiply(MOVEMENT_FACTOR_HOR);
                cameraTranslate.setY(cameraTranslate.getY() + movement.getY());
                cameraTranslate.setX(cameraTranslate.getX() + movement.getX());
                double zMovement = 0.0;
                if(keyState.isSpacePressed()) zMovement += 1.0;
                if(keyState.isCtrlPressed()) zMovement -= 1.0;
                cameraTranslate.setZ(cameraTranslate.getZ() + zMovement*MOVEMENT_FACTOR_VERT);
            }
        };
        gameLoop.start();
    }
    
    @Override
    public void handle(Event event) {
        if(event instanceof KeyEvent){
            KeyEvent keyEvent = (KeyEvent) event;
            if(keyEvent.getEventType() == KeyEvent.KEY_PRESSED || keyEvent.getEventType() == KeyEvent.KEY_RELEASED){
                switch(keyEvent.getCode()){
                    case Z:
                        //Reset camera position
                        cameraTranslate.setX(CAMERA_INITIAL_X);
                        cameraTranslate.setY(CAMERA_INITIAL_Y);
                        cameraTranslate.setZ(CAMERA_INITIAL_Z);
                        cameraRotateX.setAngle(CAMERA_INITIAL_X_ANGLE);
                        cameraRotateY.setAngle(CAMERA_INITIAL_Y_ANGLE);
                    default:
                        keyState.keyEvent(keyEvent);
                        break;
                }
            }
            
        } else if(event instanceof MouseEvent){
            MouseEvent me = (MouseEvent) event;
            if(me.getEventType() == MouseEvent.MOUSE_PRESSED){
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            } else if(me.getEventType() == MouseEvent.MOUSE_DRAGGED){
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX); 
                mouseDeltaY = (mousePosY - mouseOldY); 
                
                if (me.isPrimaryButtonDown()) {
                    cameraRotateX.setAngle(cameraRotateX.getAngle() - mouseDeltaY*MOUSE_SPEED*ROTATION_SPEED);  
                    cameraRotateY.setAngle(cameraRotateY.getAngle() + mouseDeltaX*MOUSE_SPEED*ROTATION_SPEED);  
                }
            }
        }
    }
    
    public class KeyState{
        private boolean wPressed = false;
        private boolean aPressed = false;
        private boolean sPressed = false;
        private boolean dPressed = false;
        private boolean spacePressed = false;
        private boolean ctrlPressed = false;
        
        public synchronized void keyEvent(KeyEvent keyEvent){
            if(keyEvent.getEventType() == KeyEvent.KEY_PRESSED || keyEvent.getEventType() == KeyEvent.KEY_RELEASED){
                boolean val = keyEvent.getEventType() == KeyEvent.KEY_PRESSED;
                switch (keyEvent.getCode()) {
                    case W:
                        wPressed = val;
                        break;
                    case A:
                        aPressed = val;
                        break;
                    case S:
                        sPressed = val;
                        break;
                    case D:
                        dPressed = val;
                        break;
                    case SPACE:
                        spacePressed = val;
                        break;
                    case CONTROL:
                        ctrlPressed = val;
                        break;
                }
            }
        }

        public boolean iswPressed() {
            return wPressed;
        }

        public boolean isaPressed() {
            return aPressed;
        }

        public boolean issPressed() {
            return sPressed;
        }

        public boolean isdPressed() {
            return dPressed;
        }

        public boolean isSpacePressed() {
            return spacePressed;
        }

        public boolean isCtrlPressed() {
            return ctrlPressed;
        }
        
        
    }
    
}
