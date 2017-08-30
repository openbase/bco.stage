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
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.openbase.bco.stage.Controller;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.storage.registry.SynchronizableRegistry;
import org.openbase.jul.storage.registry.SynchronizableRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.domotic.unit.UnitProbabilityType.UnitProbability;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;
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
    private static GUIManager instance;

    public final BooleanProperty axisVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty roomVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty wallVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty zoneVisibility = new SimpleBooleanProperty(false);
    public final BooleanProperty tileVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty regionVisibility = new SimpleBooleanProperty(false);
    public final BooleanProperty skeletonVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty rayVisibility = new SimpleBooleanProperty(true);
    public final BooleanProperty objectVisibility = new SimpleBooleanProperty(true);

    private final Skeleton[] skeletons = new Skeleton[6];
    private final List<PointingRay3D> rays = new ArrayList<>();
    private final SynchronizableRegistryImpl<String, ObjectBox> objectBoxRegistry;
    private final SynchronizableRegistryImpl<String, RegistryRoom> roomRegistry;

    private final Stage primaryStage;
    private final Group root = new Group();
    private final Group world = new Group();
    private final Group rayGroup = new Group();
    private final Group objectGroup = new Group();
    private final Group roomGroup = new Group();
//    private final CSRARoom room;

    public static GUIManager initInstance(Stage primaryStage) throws InstantiationException {
        if (instance == null) {
            instance = new GUIManager(primaryStage);
        }
        return instance;
    }

    public static GUIManager getInstance() {
        return instance;
    }

    private GUIManager(Stage primaryStage) throws InstantiationException {
        try {
            LOGGER.info("Setting up the 3D - scene.");
            this.primaryStage = primaryStage;
            objectBoxRegistry = new SynchronizableRegistryImpl<>();
            roomRegistry = new SynchronizableRegistryImpl<>();

            root.getChildren().add(world);
            root.setDepthTest(DepthTest.ENABLE);

            buildAxes();
//            room = new CSRARoom();
//            world.getChildren().add(room);
            world.getChildren().add(roomGroup);
            world.getChildren().add(objectGroup);
            world.getChildren().add(rayGroup);
            buildSkeletons();

//            room.visibleProperty().bind(roomVisibility);
            roomGroup.visibleProperty().bind(roomVisibility);
            rayGroup.visibleProperty().bind(rayVisibility);
            objectGroup.visibleProperty().bind(objectVisibility);

            Scene scene = new Scene(root, 1024, 768, true);
            connectCamera(scene);
            scene.setFill(Color.GREY);

            primaryStage.setTitle(JPService.getApplicationName());
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                LOGGER.info("Close called on primary stage.");
                Platform.exit();
                System.exit(0);
            });

            scene.setCamera(MoveableCamera.getInstance());
        } catch (InstantiationException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public void close() {
        LOGGER.info("close called on GUIManager");
        primaryStage.close();
    }

    public synchronized void updateSkeletonData(TrackedPostures3DFloat postures) {
        updateOrCreateSkeletons(postures);
    }

    public synchronized void updateRayData(PointingRay3DFloatDistributionCollection pointingRays) {
        updateOrCreateRays(pointingRays.getElementList().stream()
                .map((rayDistribution) -> rayDistribution.getRayList())
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private void connectCamera(Scene scene) {
        LOGGER.debug("Connecting camera to the scene.");
        scene.setOnMousePressed(MoveableCamera.getInstance());
        scene.setOnMouseDragged(MoveableCamera.getInstance());
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case X:
                    axisVisibility.set(!axisVisibility.get());
                    break;
                case R:
                    rayVisibility.set(!rayVisibility.get());
                    break;
                case P:
                    skeletonVisibility.set(!skeletonVisibility.get());
                    break;
                case M:
                    roomVisibility.set(!roomVisibility.get());
                    break;
                case F:
                    wallVisibility.set(!wallVisibility.get());
                    break;
                case O:
                    objectVisibility.set(!objectVisibility.get());
                    break;
                case H:
                    //TODO: show help here.
                    break;
                case DIGIT1:
                    zoneVisibility.set(!zoneVisibility.get());
                    break;
                case DIGIT2:
                    tileVisibility.set(!tileVisibility.get());
                    break;
                case DIGIT3:
                    regionVisibility.set(!regionVisibility.get());
                    break;
                case C:
                    try {
                        Controller.getInstance().initializeRegistryConnection();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Controller.criticalError(ex);
                    } catch (CouldNotPerformException ex) {
                        Controller.criticalError(ex);
                    }
                    break;
                default:
                    MoveableCamera.getInstance().handle(event);
                    break;
            }
        });
        scene.setOnKeyReleased(MoveableCamera.getInstance());
    }

    private void buildAxes() {
        LOGGER.debug("Creating axes visualization.");
        Group axisGroup = new Group();

        PhongMaterialManager mm = PhongMaterialManager.getInstance();
        final Line3D xLine = new Line3D(Line3D.LineType.BOX, AXIS_WIDTH, mm.red, new Point3D(-AXIS_LENGTH, 0, 0), new Point3D(AXIS_LENGTH, 0, 0));
        final Line3D yLine = new Line3D(Line3D.LineType.BOX, AXIS_WIDTH, mm.green, new Point3D(0, -AXIS_LENGTH, 0), new Point3D(0, AXIS_LENGTH, 0));
        final Line3D zLine = new Line3D(Line3D.LineType.BOX, AXIS_WIDTH, mm.blue, new Point3D(0, 0, -AXIS_LENGTH), new Point3D(0, 0, AXIS_LENGTH));

        axisGroup.getChildren().addAll(xLine, yLine, zLine);
        axisGroup.visibleProperty().bind(axisVisibility);
        world.getChildren().add(axisGroup);
    }

    private void buildSkeletons() {
        LOGGER.debug("Creating skeleton visualizations.");
        Group skeletonGroup = new Group();
        for (int i = 0; i < skeletons.length; i++) {
            skeletons[i] = new Skeleton();
            skeletonGroup.getChildren().add(skeletons[i]);
            skeletons[i].setVisible(false);
        }
        skeletonGroup.visibleProperty().bind(skeletonVisibility);
        world.getChildren().add(skeletonGroup);
    }

    private synchronized void updateOrCreateSkeletons(TrackedPostures3DFloat postures) {
        Platform.runLater(() -> {
            for (int i = 0; i < postures.getPostureCount(); i++) {
                TrackedPosture3DFloat posture = postures.getPosture(i);
                if (posture.getConfidenceCount() > 0) {
                    skeletons[i].updatePositions(posture);
                    skeletons[i].setVisible(true);
                } else {
                    skeletons[i].setVisible(false);
                }
            }
        });
    }

    private synchronized void updateOrCreateRays(List<PointingRay3DFloat> pointingRays) {
        Platform.runLater(() -> {
            LOGGER.trace("Updating or creating rays.");
            int difference = pointingRays.size() - rays.size();
            if (difference > 0) {
                LOGGER.trace("Adding new rays.");
                for (int i = 0; i < difference; i++) {
                    PointingRay3D r = new PointingRay3D();
                    rays.add(r);
                    rayGroup.getChildren().add(r);
                }
            } else {
                LOGGER.trace("Removing rays.");
                for (int i = 0; i < -difference; i++) {
                    PointingRay3D r = rays.get(pointingRays.size());
                    rayGroup.getChildren().remove(r);
                    rays.remove(pointingRays.size());
                }
            }
            LOGGER.trace("Updating existing rays.");
            for (int i = 0; i < pointingRays.size(); i++) {
                rays.get(i).update(pointingRays.get(i));
            }
        });
    }

    public Group getRoomGroup() {
        return roomGroup;
    }

    public SynchronizableRegistry<String, RegistryRoom> getRoomRegistry() {
        return roomRegistry;
    }

    public Group getObjectGroup() {
        return objectGroup;
    }

    public SynchronizableRegistry<String, ObjectBox> getObjectBoxRegistry() {
        return objectBoxRegistry;
    }

    public void highlightObjects(UnitProbabilityCollection selectedUnits) {
        for (UnitProbability up : selectedUnits.getElementList()) {
            try {
                if (objectBoxRegistry.contains(up.getId())) {
                    objectBoxRegistry.get(up.getId()).highlight(up.getProbability());
                } else {
                    LOGGER.trace("ObjectBox with id " + up.getId() + " not found in local registry.");
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not check objectBoxRegistry for id: " + up.getId(), ex), LOGGER, LogLevel.WARN);
            }
        }
    }
}
