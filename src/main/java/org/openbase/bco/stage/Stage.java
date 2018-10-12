package org.openbase.bco.stage;

/*
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
import java.util.List;
import javafx.application.Application;
import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.bco.stage.jp.JPDisableRegistry;
import org.openbase.bco.stage.jp.JPFilterPscUnits;
import org.openbase.jps.core.JPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmcastil
 */
public class Stage extends Application {

    public static final String APPLICATION_NAME = "BCO Stage";
    private static final Logger LOGGER = LoggerFactory.getLogger(Stage.class);

    @Override
    public void start(javafx.stage.Stage primaryStage) {
        /* Setup JPService */
        JPService.setApplicationName(APPLICATION_NAME);
        JPService.registerProperty(JPPSCBaseScope.class);
        JPService.registerProperty(JPPostureScope.class);
        JPService.registerProperty(JPRayScope.class);
        JPService.registerProperty(JPSelectedUnitScope.class);
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPPscUnitFilterList.class);
        JPService.registerProperty(JPDisableRegistry.class);
        JPService.registerProperty(JPFilterPscUnits.class);
        List<String> parameters = getParameters().getRaw();
        String[] args = parameters.toArray(new String[parameters.size()]);
        JPService.parseAndExitOnError(args);
//        JPService.printHelp();

        StageController.initInstance(primaryStage);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
