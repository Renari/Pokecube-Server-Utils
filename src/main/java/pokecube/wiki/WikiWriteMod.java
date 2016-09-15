package pokecube.wiki;

import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

@Mod(modid = WikiWriteMod.MODID, name = "wikiwriter", version = WikiWriteMod.VERSION, dependencies = "required-after:pokecube", acceptableRemoteVersions = "*", acceptedMinecraftVersions = WikiWriteMod.MCVERSIONS)
public class WikiWriteMod
{

    Map<PokedexEntry, Integer> genMap     = Maps.newHashMap();
    public static final String MODID      = "pokecube_wikioutput";
    public static final String VERSION    = "@VERSION@";

    public final static String MCVERSIONS = "[1.9.4]";

    @Instance(value = MODID)
    public static WikiWriteMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandUsage(ICommandSender sender)
            {
                return "/pokewiki stuff";
            }

            @Override
            public String getCommandName()
            {
                return "pokewiki";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                if (args.length == 1 && args[0].equals("all"))
                {
                    PokecubeWikiWriter.writeWiki();
                }
                else PokecubeWikiWriter.outputPokemonWikiInfo2(Database.getEntry("arceus"));
            }
        });
    }

    @SidedProxy
    public static CommonProxy proxy;

    public static class CommonProxy implements IGuiHandler
    {
        void setupModels()
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @SubscribeEvent
    public void keyInput(KeyInputEvent evt)
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_DELETE) && Minecraft.getMinecraft().currentScreen == null)
        {
            System.out.println("Test");
            PokecubeWikiWriter.gifs = false;// Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
            GuiGifCapture.shiny = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
            System.out.println(GuiGifCapture.shiny + " ");
            PokecubeWikiWriter.beginGifCapture();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_HOME) && Minecraft.getMinecraft().currentScreen == null)
        {
            GuiGifCapture.shiny = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
            GuiGifCapture.pokedexEntry = Database.getEntry(1);
            System.out.println(GuiGifCapture.shiny + " ");
            Minecraft.getMinecraft().thePlayer.openGui(WikiWriteMod.instance, 0,
                    Minecraft.getMinecraft().thePlayer.getEntityWorld(), 0, 0, 0);
        }
    }

    public static class ServerProxy extends CommonProxy
    {
    }

    public static class ClientProxy extends CommonProxy
    {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new GuiGifCapture(null, player);
        }
    }
}
