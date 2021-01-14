package org.openbase.bco.stage.registry;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.stage.visualization.RegistryRoom;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryRoomFactory implements Factory<RegistryRoom, UnitConfigType.UnitConfig> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryRoomFactory.class);
    private static RegistryRoomFactory instance;

    private RegistryRoomFactory() {
    }

    /**
     * Method returns a new singelton instance of the unit factory.
     *
     * @return
     */
    public synchronized static RegistryRoomFactory getInstance() {
        if (instance == null) {
            instance = new RegistryRoomFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     *
     * @param config {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InstantiationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public RegistryRoom newInstance(UnitConfigType.UnitConfig config) throws InstantiationException, InterruptedException {
        try {
            RegistryRoom room = new RegistryRoom();
            room.applyConfigUpdate(config);
            LOGGER.info("Created room for unit " + config.getLabel() + " with id " + config.getId());
            return room;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(RegistryRoom.class, ex);
        }
    }

}
