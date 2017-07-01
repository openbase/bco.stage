/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.visualization;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;

/**
 *
 * @author thoren
 */
public class Room extends Group {
    private static final double ROOM_HEIGHT = 2.80;
    private static final double DEFAULT_WIDTH = 0.05;
    private static final Point3D TO_CEIL = new Point3D(0, 0, ROOM_HEIGHT);
    private final List<Point3D> groundPoints = new ArrayList<>();
    
    private void fillGroundPoints(){
        groundPoints.add(new Point3D(0, 0, 0));
        groundPoints.add(new Point3D(0, 8.8, 0));
        groundPoints.add(new Point3D(3.75, 8.8, 0));
        groundPoints.add(new Point3D(3.75, 3.95, 0));
        groundPoints.add(new Point3D(4.2, 3.95, 0));
        groundPoints.add(new Point3D(4.2, 0, 0));
        groundPoints.add(new Point3D(2.42, 0, 0));
        groundPoints.add(new Point3D(2.42, 4.45, 0));
        groundPoints.add(new Point3D(2.42, 0, 0));
        groundPoints.add(groundPoints.get(0));
    }
    
    public Room(double width){
        PhongMaterial material = new MaterialManager().white;
        Line.LineType lineType = Line.LineType.BOX;
        fillGroundPoints();
        for(int i = 0; i < groundPoints.size() -1; i++){
            Point3D start = groundPoints.get(i);
            Point3D startUp = start.add(TO_CEIL);
            Point3D end = groundPoints.get(i+1);
            Point3D endUp = end.add(TO_CEIL);
            super.getChildren().add(new Line(lineType, width, material, start, end));
            super.getChildren().add(new Line(lineType, width, material, start, startUp));
            super.getChildren().add(new Line(lineType, width, material, startUp, endUp));
        }
    }
    
    public Room(){
        this(DEFAULT_WIDTH);
    }
}
