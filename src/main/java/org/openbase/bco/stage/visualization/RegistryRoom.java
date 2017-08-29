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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.stage.registry.JavaFX3dObjectRegistryEntry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.math.Vec3DDoubleType.Vec3DDouble;
import rst.spatial.ShapeType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryRoom implements JavaFX3dObjectRegistryEntry<String, UnitConfig> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryRoom.class);
    private final Room room;

    private UnitConfig config;

    /**
     * Constructor.
     */
    public RegistryRoom() {
        room = new Room();
        room.setVisible(false);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public synchronized Node getNode() {
        return room;
    }

    /**
     * {@inheritDoc}
     *
     * @param config {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public synchronized UnitConfig applyConfigUpdate(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        this.config = config;
        switch (config.getLocationConfig().getType()) {
            case ZONE:
                room.visibleProperty().bind(GUIManager.getInstance().zoneVisibility);
                break;
            case TILE:
                room.visibleProperty().bind(GUIManager.getInstance().tileVisibility);
                break;
            case REGION:
                room.visibleProperty().bind(GUIManager.getInstance().regionVisibility);
                break;
            default:
                room.visibleProperty().unbind();
                break;
        }
        ShapeType.Shape shape = config.getPlacementConfig().getShape();
        Transform3D inverseTransform;
        try {
            inverseTransform = Units.getUnitTransformation(config).get().getTransform();
            inverseTransform.invert();
            List<Point3D> floorPoints = transformPositions(shape.getFloorList(), inverseTransform);
            List<Point3D> ceilingPoints = transformPositions(shape.getCeilingList(), inverseTransform);
            room.setConnections(floorPoints, ceilingPoints, shape.getFloorCeilingEdgeList());
        } catch (ExecutionException ex) {
            throw new CouldNotPerformException("applyConfigUpdate failed.", ex);
        }
        return config;
    }

    private List<Point3D> transformPositions(List<Vec3DDouble> positions, Transform3D transform) {
        return positions.stream()
                .map((vec) -> new Point3d(vec.getX(), vec.getY(), vec.getZ()))
                .map(point -> {
                    transform.transform(point);
                    return point;
                })
                .map(point -> new Point3D(point.x, point.y, point.z))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized String getId() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Id");
        }
        return config.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized UnitConfig getConfig() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Config");
        }
        return config;
    }

}
