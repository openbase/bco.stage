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
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Room extends Group {
    private static final double ROOM_HEIGHT = 3.20;
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
        PhongMaterial material = MaterialManager.getInstance().grey;
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
