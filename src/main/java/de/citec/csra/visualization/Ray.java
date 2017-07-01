/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.visualization;

import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import rst.math.Vec3DFloatType;
import rst.tracking.PointingRay3DFloatType;

/**
 *
 * @author thoren
 */
public class Ray extends Line {
    private final static MaterialManager MATERIAL_MANAGER = new MaterialManager();
    private final static double RAY_LENGTH = 10;
    private final static double MIN_WIDTH = 0.002;
    private final static double MAX_WIDTH = 0.02;
    private final static double WIDTH_SPAN = MAX_WIDTH-MIN_WIDTH;
    
    private Point3D VecToPoint(Vec3DFloatType.Vec3DFloat vector){
        return new Point3D(vector.getX(), vector.getY(), vector.getZ());
    }
    
    public Ray() {
        super(LineType.CYLINDER, 0, MATERIAL_MANAGER.white);
    }
    
    public void update(PointingRay3DFloatType.PointingRay3DFloat ray){
        Material material;
        switch(ray.getType()){
            case HEAD_HAND:
                material = MATERIAL_MANAGER.red;
                break;
            case SHOULDER_HAND:
                material = MATERIAL_MANAGER.blue;
                break;
            case FOREARM:
                material = MATERIAL_MANAGER.green;
                break;
            case HEAD_FINGERTIP:
                material = MATERIAL_MANAGER.cyan;
                break;
            case HAND:
                material = MATERIAL_MANAGER.margenta;
                break;
            default:
                material = MATERIAL_MANAGER.white;
        }
        super.setMaterial(material);
        Point3D origin = VecToPoint(ray.getRay().getOrigin());
        Point3D direction = VecToPoint(ray.getRay().getDirection());
        Point3D end = origin.add(direction.normalize().multiply(RAY_LENGTH));
        super.setStartEndPoints(origin, end);
        super.setWidth(ray.getCertainty()*WIDTH_SPAN + MIN_WIDTH);
    }
    
}
