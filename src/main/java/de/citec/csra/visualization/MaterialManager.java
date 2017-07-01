/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.visualization;

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
