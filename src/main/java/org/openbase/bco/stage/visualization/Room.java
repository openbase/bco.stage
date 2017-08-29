package org.openbase.bco.stage.visualization;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import rst.spatial.FloorCeilingEdgeIndicesType.FloorCeilingEdgeIndices;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Room extends Group {
    private static final double DEFAULT_WIDTH = 0.05;
    private static final Material DEFAULT_MATERIAL = PhongMaterialManager.getInstance().grey;
    private static final Line3D.LineType DEFAULT_LINE_TYPE = Line3D.LineType.BOX;
    private final double width;
    private final Material material;
    private final Line3D.LineType lineType;
    
    private Room(final double width, final Material material, final Line3D.LineType lineType) {
        this.width = width;
        this.material = material;
        this.lineType = lineType;
    }
    
    public Room() {
        this(DEFAULT_WIDTH, DEFAULT_MATERIAL, DEFAULT_LINE_TYPE);
    }
    
    public void setConnections(final List<Point3D> floorPoints, final List<Point3D> ceilingPoints, final List<FloorCeilingEdgeIndices> edgeIndices) {
        Platform.runLater(() -> {
            super.getChildren().clear();
            BooleanProperty prop;
            addOutline(floorPoints, false);
            addOutline(ceilingPoints, true);
            edgeIndices.stream().forEachOrdered(indices -> {
                addConnection(floorPoints.get((int) indices.getFloorIndex()), ceilingPoints.get((int) indices.getCeilingIndex()), GUIManager.getInstance().wallVisibility);
            });
        });
    }
    
    private void addOutline(List<Point3D> pointList, boolean ceiling) {
        for(int i = 0; i < pointList.size() - 1; i++){
            if(ceiling)
                addConnection(pointList.get(i), pointList.get(i+1), GUIManager.getInstance().wallVisibility);
            else 
                addConnection(pointList.get(i), pointList.get(i+1));
        }
        if(!pointList.isEmpty()) {
            if(ceiling)
                addConnection(pointList.get(pointList.size() - 1), pointList.get(0), GUIManager.getInstance().wallVisibility);
            else
                addConnection(pointList.get(pointList.size() - 1), pointList.get(0));
        }
    }
    
    private void addConnection(final Point3D start, final Point3D end, final BooleanProperty visiblityProperty) {
        Line3D line = getLine(start, end);
        line.visibleProperty().bind(visiblityProperty);
        super.getChildren().add(line);
    }
    
    private void addConnection(final Point3D start, final Point3D end) {
        super.getChildren().add(getLine(start, end));
    }
    
    private Line3D getLine(final Point3D start, final Point3D end) {
        return new Line3D(lineType, width, material, start, end);
    }
}
