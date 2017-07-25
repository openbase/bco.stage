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

import java.util.HashMap;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.registry.RegistryImpl;
import org.openbase.jul.storage.registry.SynchronizableRegistry;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public class SynchronizableRegistryImpl<KEY, ENTRY extends Identifiable<KEY>> extends RegistryImpl<KEY, ENTRY> implements SynchronizableRegistry<KEY, ENTRY>{

    private static final long NEVER_SYNCHRONIZED = -1;

    private long lastSynchronizationTimestamp = NEVER_SYNCHRONIZED;

    public SynchronizableRegistryImpl() throws org.openbase.jul.exception.InstantiationException {
        super(new HashMap<>());
    }

    public SynchronizableRegistryImpl(HashMap<KEY, ENTRY> entryMap) throws org.openbase.jul.exception.InstantiationException {
        super(entryMap);
    }

    @Override
    public void notifySynchronization() {
        lastSynchronizationTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean isInitiallySynchronized() {
        return lastSynchronizationTimestamp != NEVER_SYNCHRONIZED;
    }

    @Override
    public long getLastSynchronizationTimestamp() throws NotAvailableException {
        if (!isInitiallySynchronized()) {
            throw new NotAvailableException("SynchronizationTimestamp", new InvalidStateException("ControllerRegistry was never fully synchronized yet!"));
        }
        return lastSynchronizationTimestamp;
    }
    
}
