package pokecube.serverutils;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    private static final String capture                  = "captureRestriction";
    private static final String dimensionControl         = "dimensionControl";

    @Configure(category = capture)
    int                         maxCaptureLevelNormal    = 100;
    @Configure(category = capture)
    int                         maxCaptureLevelLegendary = 100;
    @Configure(category = capture)
    String[]                    maxCaptureLevelOverrides = { "mew:100" };

    @Configure(category = dimensionControl)
    int[]                       dimensions               = {};
    @Configure(category = dimensionControl)
    boolean                     enabled                  = false;
    @Configure(category = dimensionControl)
    boolean                     whitelist                = false;
    @Configure(category = dimensionControl)
    boolean                     blacklist                = false;

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
