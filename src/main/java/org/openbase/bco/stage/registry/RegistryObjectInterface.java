/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.stage.registry;

import rct.Transform;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public interface RegistryObjectInterface {

    public void add(UnitConfigType.UnitConfig config, Transform toRootCoordinateTransform);

    public void update(String id, UnitConfigType.UnitConfig config, Transform toRootCoordinateTransform);

    public void removeById(String id);
    
    public void shutdownRemotes();
}
