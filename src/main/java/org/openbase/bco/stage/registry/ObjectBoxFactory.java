package org.openbase.bco.stage.registry;

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

import org.openbase.bco.stage.visualization.ObjectBox;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ObjectBoxFactory implements Factory<ObjectBox, UnitConfigType.UnitConfig>  {
    public static ObjectBoxFactory instance;
    
    private ObjectBoxFactory(){}

    /**
     * Method returns a new singelton instance of the unit factory.
     *
     * @return
     */
    public synchronized static ObjectBoxFactory getInstance() {
        if (instance == null) {
            instance = new ObjectBoxFactory();
        }
        return instance;
    }

    @Override
    public ObjectBox newInstance(UnitConfigType.UnitConfig config) throws InstantiationException, InterruptedException {
        try {
            ObjectBox box = new ObjectBox();
            box.applyConfigUpdate(config);
            return box;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("ObjectBoxInstance", ex);
        }
    }
    
}
