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

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 *
 * @author thoren
 */
public class MaterialManager {
    private ArrayList<PhongMaterial> skeletonMaterials = new ArrayList<>();
    private int index = 0;
    public PhongMaterial red;
    public PhongMaterial blue;
    public PhongMaterial green;
    public PhongMaterial margenta;
    public PhongMaterial cyan;
    public PhongMaterial orange;
    public PhongMaterial grey;
    public PhongMaterial gray;
    public PhongMaterial white;

    public MaterialManager() {
        red = new PhongMaterial(Color.DARKRED);
        red.setSpecularColor(Color.RED);
        skeletonMaterials.add(red);
        
        blue = new PhongMaterial(Color.DARKBLUE);
        blue.setSpecularColor(Color.BLUE);
        skeletonMaterials.add(blue);
        
        green = new PhongMaterial(Color.DARKGREEN);
        green.setSpecularColor(Color.GREEN);
        skeletonMaterials.add(green);

        margenta = new PhongMaterial(Color.DARKMAGENTA);
        margenta.setSpecularColor(Color.MAGENTA);
        skeletonMaterials.add(margenta);
        
        cyan = new PhongMaterial(Color.DARKCYAN);
        cyan.setSpecularColor(Color.CYAN);
        skeletonMaterials.add(cyan);
        
        orange = new PhongMaterial(Color.DARKORANGE);
        orange.setSpecularColor(Color.ORANGE);
        skeletonMaterials.add(orange);

        grey = new PhongMaterial(Color.DARKGREY);
        grey.setSpecularColor(Color.GREY);
        
        white = new PhongMaterial(Color.WHITESMOKE);
        white.setSpecularColor(Color.WHITE);
    }
    
    public PhongMaterial nextSkeletonMaterial(){
        PhongMaterial material = skeletonMaterials.get(index++);
        if(index >= skeletonMaterials.size()) index = 0;
        return material;
    }
    
}
