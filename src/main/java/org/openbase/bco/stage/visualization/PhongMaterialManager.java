package org.openbase.bco.stage.visualization;

/*
 * -
 * #%L
 * BCO Stage
 * %%
 * Copyright (C) 2017 - 2021 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PhongMaterialManager {

    private static PhongMaterialManager instance;

    private final ArrayList<PhongMaterial> skeletonMaterials = new ArrayList<>();
    private int index = 0;
    public final PhongMaterial red;
    public final PhongMaterial blue;
    public final PhongMaterial green;
    public final PhongMaterial margenta;
    public final PhongMaterial cyan;
    public final PhongMaterial yellow;
    public final PhongMaterial orange;
    public final PhongMaterial grey;
    public final PhongMaterial white;

    private PhongMaterialManager() {
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

        yellow = new PhongMaterial(Color.YELLOW);
        yellow.setSpecularColor(Color.LIGHTYELLOW);
        skeletonMaterials.add(yellow);

        orange = new PhongMaterial(Color.DARKORANGE);
        orange.setSpecularColor(Color.ORANGE);
        skeletonMaterials.add(orange);

        grey = new PhongMaterial(Color.DARKGREY);
        grey.setSpecularColor(Color.GREY);

        white = new PhongMaterial(Color.WHITESMOKE);
        white.setSpecularColor(Color.WHITE);
    }

    /**
     * Method returns a new singelton instance of the unit factory.
     *
     * @return
     */
    public synchronized static PhongMaterialManager getInstance() {
        if (instance == null) {
            instance = new PhongMaterialManager();
        }
        return instance;
    }

    public PhongMaterial getSkeletonMaterial(int index) {
        while (index >= skeletonMaterials.size()) {
            index -= skeletonMaterials.size();
        }
        return skeletonMaterials.get(index);
    }

    public PhongMaterial nextSkeletonMaterial() {
        if (index >= skeletonMaterials.size()) {
            index = 0;
        }
        return getSkeletonMaterial(index++);
    }

}
