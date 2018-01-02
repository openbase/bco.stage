package org.openbase.bco.stage.visualization;

/*-
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 - 2018 openbase.org
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
import rst.spatial.FloorCeilingEdgeIndicesType.FloorCeilingEdgeIndices;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class CSRARoom extends Room {

    private static final double ROOM_HEIGHT = 3.20;
    private static final Point3D TO_CEIL = new Point3D(0, 0, ROOM_HEIGHT);
    private final List<Point3D> groundPoints = new ArrayList<>();
    private final List<Point3D> ceilingPoints = new ArrayList<>();
    private final List<FloorCeilingEdgeIndices> edgeIndices = new ArrayList<>();

    public CSRARoom() {
        fillGroundPoints();
        fillCeilingAndEdges();
        super.setConnections(groundPoints, ceilingPoints, edgeIndices);
    }

    private void fillGroundPoints() {
        groundPoints.add(new Point3D(0, 0, 0));
        groundPoints.add(new Point3D(0, 8.8, 0));
        groundPoints.add(new Point3D(3.75, 8.8, 0));
        groundPoints.add(new Point3D(3.75, 3.95, 0));
        groundPoints.add(new Point3D(4.2, 3.95, 0));
        groundPoints.add(new Point3D(4.2, 0, 0));
        groundPoints.add(new Point3D(2.42, 0, 0));
        groundPoints.add(new Point3D(2.42, 4.45, 0));
        groundPoints.add(new Point3D(2.42, 0, 0));
    }

    private void fillCeilingAndEdges() {
        for (int i = 0; i < groundPoints.size(); i++) {
            edgeIndices.add(FloorCeilingEdgeIndices.newBuilder().setFloorIndex(i).setCeilingIndex(i).build());
            ceilingPoints.add(groundPoints.get(i).add(TO_CEIL));
        }
    }
}
