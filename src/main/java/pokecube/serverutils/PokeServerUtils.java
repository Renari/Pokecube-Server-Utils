package pokecube.serverutils;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.IAIRunnable;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.events.InitAIEvent;
import pokecube.core.events.MoveUse;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.serverutils.ai.pokemob.AITurnAttack;

@Mod(modid = PokeServerUtils.MODID, name = "Pokecube Server Utils", version = PokeServerUtils.VERSION, dependencies = "required-after:pokecube", acceptableRemoteVersions = "*")
public class PokeServerUtils
{
    public static final String             MODID         = "pokecubeserverutils";
    public static final String             VERSION       = "1.0.0";

    @Instance(value = MODID)
    public static PokeServerUtils          instance;
    Config                                 config;

    private HashMap<PokedexEntry, Integer> overrides     = Maps.newHashMap();
    Set<Integer>                           dimensionList = Sets.newHashSet();

    public PokeServerUtils()
    {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        config = new Config(PokecubeCore.core.getPokecubeConfig(e).getConfigFile());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new SettingsCommand());
    }

    @SubscribeEvent
    public void canCapture(CaptureEvent.Pre evt)
    {
        int level = evt.caught.getLevel();
        boolean legendary = evt.caught.getPokedexEntry().legendary;
        int max = overrides.containsKey(evt.caught.getPokedexEntry()) ? overrides.get(evt.caught.getPokedexEntry())
                : legendary ? config.maxCaptureLevelLegendary : config.maxCaptureLevelNormal;
        if (level > max)
        {
            evt.setCanceled(true);
            evt.setResult(Result.DENY);
            Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
            if (catcher instanceof EntityPlayer)
            {
                ((EntityPlayer) catcher).addChatMessage(new TextComponentTranslation("pokecube.denied"));
            }
            evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getEntityItem(), (float) 0.5);
            evt.pokecube.setDead();
        }
    }

    @SubscribeEvent
    public void postpostInit(PostPostInit evt)
    {
        for (String s : config.maxCaptureLevelOverrides)
        {
            String[] args = s.split(":");
            try
            {
                int level = Integer.parseInt(args[1]);
                PokedexEntry entry = Database.getEntry(args[0]);
                if (entry == null)
                {
                    PokecubeMod.log(args[0] + " not found in database");
                }
                else
                {
                    overrides.put(entry, level);
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log("Error with " + s);
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onSendOut(SendOut.Pre evt)
    {
        if (!config.dimsEnabled) return;
        int dim = evt.world.provider.getDimension();
        boolean inList = dimensionList.contains(dim);
        if (config.whitelist)
        {
            if (!inList)
            {
                evt.setCanceled(true);
            }
        }
        else if (config.blacklist)
        {
            if (inList)
            {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onAttackCommand(CommandAttackEvent event)
    {
        if (!config.turnbased || !event.getPokemob().getPokemonAIState(IPokemob.ANGRY)) return;
        for (IAIRunnable ai : event.getPokemob().getAIStuff().aiTasks)
        {
            if (ai instanceof AITurnAttack)
            {
                AITurnAttack task = (AITurnAttack) ai;
                task.hasOrders = true;
                return;
            }
        }
    }

    @SubscribeEvent
    public void onAttackCommand(InitAIEvent event)
    {
        if (!config.turnbased || event.getPokemob().getAIStuff() == null) return;
        AITurnAttack attack = new AITurnAttack((EntityLiving) event.getEntity());
        for (IAIRunnable ai : event.getPokemob().getAIStuff().aiTasks)
        {
            if (ai instanceof AIAttack)
            {
                AIAttack old = (AIAttack) ai;
                event.getPokemob().getAIStuff().aiTasks.remove(old);
                attack.setMutex(old.getMutex());
                attack.setPriority(old.getPriority());
                event.getPokemob().getAIStuff().aiTasks.add(0, attack);
                break;
            }
        }

    }

    @SubscribeEvent
    public void onAttackUse(MoveUse.ActualMoveUse.Post event)
    {
        if (!config.turnbased||  !(event.getTarget() instanceof IPokemob))
            return;
        System.out.println("tick");
        for (IAIRunnable ai : event.getUser().getAIStuff().aiTasks)
        {
            if (ai instanceof AITurnAttack)
            {
                AITurnAttack task = (AITurnAttack) ai;
                task.hasOrders = false;
                break;
            }
        }
        for (IAIRunnable ai : ((IPokemob) event.getTarget()).getAIStuff().aiTasks)
        {
            if (ai instanceof AITurnAttack)
            {
                AITurnAttack task = (AITurnAttack) ai;
                task.hasOrders = false;
                break;
            }
        }
    }
}
