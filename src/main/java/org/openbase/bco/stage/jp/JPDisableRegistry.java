package org.openbase.bco.stage.jp;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPDisableRegistry extends org.openbase.jps.preset.AbstractJPBoolean {
    public final static String[] COMMAND_IDENTIFIERS = {"-dr", "--disable-registry"};

    public JPDisableRegistry() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "If true, the program will not connect to the registry and thus not load any unit objects.";
    }
    
}
