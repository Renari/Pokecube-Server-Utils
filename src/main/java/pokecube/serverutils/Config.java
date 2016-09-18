package pokecube.serverutils;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    private static final String CAPTURE                  = "captureRestriction";
    private static final String DIMCTRL                  = "dimensionControl";
    private static final String CBTMODE                  = "battlemode";

    @Configure(category = CAPTURE)
    int                         maxCaptureLevelNormal    = 100;
    @Configure(category = CAPTURE)
    int                         maxCaptureLevelLegendary = 100;
    @Configure(category = CAPTURE)
    String[]                    maxCaptureLevelOverrides = { "mew:100" };

    @Configure(category = DIMCTRL)
    int[]                       dimensions               = {};
    @Configure(category = DIMCTRL)
    boolean                     dimsEnabled              = false;
    @Configure(category = DIMCTRL)
    boolean                     whitelist                = false;
    @Configure(category = DIMCTRL)
    boolean                     blacklist                = false;

    @Configure(category = CBTMODE)
    boolean                     turnbased                = false;

    public Config()
    {
        super(null);
    }

    public Config(File configFile)
    {
        super(configFile, new Config());
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
        PokeServerUtils.instance.dimensionList.clear();
        for (int i : dimensions)
            PokeServerUtils.instance.dimensionList.add(i);
    }

}
