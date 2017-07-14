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

import javafx.geometry.Point3D;
import rst.geometry.Ray3DFloatType.Ray3DFloat;
import rst.math.Vec3DFloatType.Vec3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Ray extends Line {
    private final static double RAY_LENGTH = 10;
    private final double rayLength;
    
    private Point3D VecToPoint(Vec3DFloat vector){
        return new Point3D(vector.getX(), vector.getY(), vector.getZ());
    }
    
    public Ray() {
        super(Line.LineType.CYLINDER, 0, PhongMaterialManager.getInstance().white);
        this.rayLength = RAY_LENGTH;
    }
    
    public Ray(double rayLength) {
        super(Line.LineType.CYLINDER, 0, PhongMaterialManager.getInstance().white);
        this.rayLength = rayLength;
    }
    
    public void update(Ray3DFloat ray){
        Point3D origin = VecToPoint(ray.getOrigin());
        Point3D direction = VecToPoint(ray.getDirection());
        Point3D end = origin.add(direction.normalize().multiply(rayLength));
        super.setStartEndPoints(origin, end);
    }
    
}
