package org.openbase.bco.stage.jp;

import org.openbase.jps.exception.JPNotAvailableException;
import rsb.Scope;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPSelectedUnitScope extends AbstractJPScope {
    public final static String[] COMMAND_IDENTIFIERS = {"--us", "--unit-scope"};
    
    public JPSelectedUnitScope(){
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "Defines the scope used to receive units selected by pointing and their probabilities.";
    }

    @Override
    protected Scope getPropertyDefaultValue() throws JPNotAvailableException {
        return new Scope("/selected_units");
    }
    
}
