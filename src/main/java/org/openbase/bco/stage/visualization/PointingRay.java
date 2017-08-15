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

import javafx.scene.paint.Material;
import rst.tracking.PointingRay3DFloatType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PointingRay extends Ray {
    private final static double MIN_WIDTH = 0.002;
    private final static double MAX_WIDTH = 0.02;
    private final static double WIDTH_SPAN = MAX_WIDTH-MIN_WIDTH;
    
    public PointingRay() {
        super();
    }
    
    public PointingRay(double rayLength) {
        super(rayLength);
    }
    
    public void update(PointingRay3DFloatType.PointingRay3DFloat ray){
        super.update(ray.getRay());
        Material material;
        switch(ray.getType()){
            case HEAD_HAND:
                material = PhongMaterialManager.getInstance().red;
                break;
            case SHOULDER_HAND:
                material = PhongMaterialManager.getInstance().blue;
                break;
            case FOREARM:
                material = PhongMaterialManager.getInstance().green;
                break;
            case HEAD_FINGERTIP:
                material = PhongMaterialManager.getInstance().cyan;
                break;
            case HAND:
                material = PhongMaterialManager.getInstance().margenta;
                break;
            case OTHER:
                material = PhongMaterialManager.getInstance().red;
                break;
            default:
                material = PhongMaterialManager.getInstance().white;
                break;
        }
        super.setMaterial(material);
        super.setWidth(ray.getCertainty()*WIDTH_SPAN + MIN_WIDTH);
    }
    
}
