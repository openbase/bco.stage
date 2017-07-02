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

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;

/**
 *
 * @author thoren
 */
public class Line extends Group {
    private final static Point3D UP = new Point3D(0, 1, 0);
    private Box box;
    private Cylinder cylinder;
    private LineType type;
    
    public enum LineType{
        BOX,
        CYLINDER
    }
    
    public Line(LineType type, double width, Material material){
        this.type = type;
        super.setVisible(false);
        switch(type){
            case BOX:
                box = new Box(width, 0, width);
                box.setMaterial(material);
                super.getChildren().add(box);
                break;
            case CYLINDER:
                cylinder = new Cylinder(width*0.5, 0);
                cylinder.setMaterial(material);
                super.getChildren().add(cylinder);
                break;
                
        }
    }
    
    public Line(LineType type, double width, Material material, Point3D point1, Point3D point2){
        this(type, width, material);
        setStartEndPoints(point1, point2);
    }
    
    public final void setStartEndPoints(Point3D point1, Point3D point2){
        super.setVisible(true);
        Point3D direction = point1.subtract(point2);
        Point3D position = point1.midpoint(point2);
        setLength(direction.magnitude());
        super.setTranslateX(position.getX());
        super.setTranslateY(position.getY());
        super.setTranslateZ(position.getZ());
        Point3D axis = UP.crossProduct(direction.normalize());
        super.setRotationAxis(axis);
        super.setRotate(UP.angle(direction.normalize()));
    }
    
    public void setMaterial(Material material){
        switch(type){
            case BOX:
                box.setMaterial(material);
                break;
            case CYLINDER:
                cylinder.setMaterial(material);
                break;
        }
    }
    
    public void setWidth(double width){
        switch(type){
            case BOX:
                box.setWidth(width);
                box.setDepth(width);
                break;
            case CYLINDER:
                cylinder.setRadius(width*0.5);
                break;
        }
    }
    
    private void setLength(double length){
        switch(type){
            case BOX:
                box.setHeight(length);
                break;
            case CYLINDER:
                cylinder.setHeight(length);
                break;
        }
    }
}
