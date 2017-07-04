/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import org.openbase.bco.stage.pointing.Joints;
import org.openbase.bco.stage.pointing.JointPair;
import java.util.List;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import rst.geometry.TranslationType;
import rst.tracking.TrackedPosture3DFloatType;

/**
 *
 * @author thoren
 */
public class Skeleton extends Group{
    private final static MaterialManager MATERIAL_MANAGER = new MaterialManager();
    private final static double JOINT_SIZE = 0.04;
    private final static double HEAD_SIZE = 0.10;
    private final static double CONNECTION_DIAMETER = 0.03;
    private final static JointPair[] JOINT_PAIRS = {new JointPair(Joints.Head, Joints.Neck), new JointPair(Joints.Neck, Joints.SpineShoulder), 
        new JointPair(Joints.SpineShoulder, Joints.SpineMid), new JointPair(Joints.SpineMid, Joints.SpineBase), 
        new JointPair(Joints.SpineShoulder, Joints.ShoulderRight), new JointPair(Joints.ShoulderRight, Joints.ElbowRight), 
        new JointPair(Joints.ElbowRight, Joints.WristRight), new JointPair(Joints.WristRight, Joints.HandRight), 
        new JointPair(Joints.HandRight, Joints.HandTipRight), new JointPair(Joints.HandRight, Joints.ThumbRight), 
        new JointPair(Joints.SpineShoulder, Joints.ShoulderLeft), new JointPair(Joints.ShoulderLeft, Joints.ElbowLeft), 
        new JointPair(Joints.ElbowLeft, Joints.WristLeft), new JointPair(Joints.WristLeft, Joints.HandLeft), 
        new JointPair(Joints.HandLeft, Joints.HandTipLeft), new JointPair(Joints.HandLeft, Joints.ThumbLeft), 
        new JointPair(Joints.SpineBase, Joints.HipRight), new JointPair(Joints.HipRight, Joints.KneeRight), 
        new JointPair(Joints.KneeRight, Joints.AnkleRight), new JointPair(Joints.AnkleRight, Joints.FootRight), 
        new JointPair(Joints.SpineBase, Joints.HipLeft), new JointPair(Joints.HipLeft, Joints.KneeLeft), 
        new JointPair(Joints.KneeLeft, Joints.AnkleLeft), new JointPair(Joints.AnkleLeft, Joints.FootLeft)};
    
    private final Sphere[] spheres;
    private final Line[] lines;
    

    public Skeleton() {
        Material material = MATERIAL_MANAGER.nextSkeletonMaterial();
        spheres = new Sphere[25];
        lines = new Line[JOINT_PAIRS.length];
        for(Joints j : Joints.values()){
            if(j == Joints.Head){
                spheres[j.getValue()] = new Sphere(HEAD_SIZE);
            } else {
                spheres[j.getValue()] = new Sphere(JOINT_SIZE);
            }
            spheres[j.getValue()].setMaterial(material);
            super.getChildren().add(spheres[j.getValue()]);
        }
        for(int i = 0; i < JOINT_PAIRS.length; i++){
            lines[i] = new Line(Line.LineType.CYLINDER, CONNECTION_DIAMETER, material);
            super.getChildren().add(lines[i]);
        }
    }
    
    public void updatePositions(TrackedPosture3DFloatType.TrackedPosture3DFloat posture){
        if(posture.getConfidenceCount() > 0){
            List<TranslationType.Translation> positionList = posture.getPosture().getPositionList();
            for(int i = 0; i < positionList.size(); i++){
                // Updating the joint positions
                Point3D position = translationToPoint(positionList.get(i));
                spheres[i].setTranslateX(position.getX());
                spheres[i].setTranslateY(position.getY());
                spheres[i].setTranslateZ(position.getZ());
            }
            for(int i = 0; i < lines.length; i++){
                // Updating the joint connections.
                Point3D joint1 = translationToPoint(positionList.get(JOINT_PAIRS[i].getJoint1().getValue()));
                Point3D joint2 = translationToPoint(positionList.get(JOINT_PAIRS[i].getJoint2().getValue()));
                lines[i].setStartEndPoints(joint1, joint2);
            }
        }
    }
    
    private Point3D translationToPoint(TranslationType.Translation translation){
        return new Point3D(translation.getX(), translation.getY(), translation.getZ());
    }
}
