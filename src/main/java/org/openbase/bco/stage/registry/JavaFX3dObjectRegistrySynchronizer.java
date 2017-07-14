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

import com.google.protobuf.GeneratedMessage;
import javafx.application.Platform;
import javafx.scene.Group;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.openbase.jul.storage.registry.RemoteRegistry;
import org.openbase.jul.storage.registry.SynchronizableRegistry;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class JavaFX3dObjectRegistrySynchronizer<KEY, ENTRY extends JavaFX3dObjectRegistryEntry<KEY, CONFIG_M>, CONFIG_M extends GeneratedMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> 
        extends RegistrySynchronizer<KEY, ENTRY, CONFIG_M, CONFIG_MB> {
    Group objectGroup;
    
    public JavaFX3dObjectRegistrySynchronizer(Group objectGroup, SynchronizableRegistry<KEY, ENTRY> registry, RemoteRegistry<KEY, CONFIG_M, CONFIG_MB> remoteRegistry, Factory<ENTRY, CONFIG_M> factory) throws InstantiationException {
        super(registry, remoteRegistry, factory);
        this.objectGroup = objectGroup;
    }

//    @Override
//    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
//        ENTRY entry = super.update(config);
//        return entry;
//    }

    @Override
    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.register(config);
        Platform.runLater(new Runnable() {
            @Override public void run() {
                objectGroup.getChildren().add(entry.getNode());
            }
        });
        return entry;
    }

    @Override
    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.remove(config);
        Platform.runLater(new Runnable() {
            @Override public void run() {
                objectGroup.getChildren().remove(entry.getNode());
            }
        });
        return entry;
    }
    
    

//    @Override
//    public void deactivate() throws CouldNotPerformException, InterruptedException {
//        super.deactivate();
//
////        for (ObjectBox entry : localRegistry.getEntries()) {
////            entry.disable();
////        }
//    }
    
    @Override
    public abstract boolean verifyConfig(final CONFIG_M config) throws VerificationFailedException;
}
