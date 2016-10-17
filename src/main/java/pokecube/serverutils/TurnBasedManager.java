package pokecube.serverutils;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.ai.thread.IAIRunnable;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.events.CommandAttackEvent;
import pokecube.core.events.InitAIEvent;
import pokecube.core.events.MoveUse;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.serverutils.ai.pokemob.AITurnAttack;

public class TurnBasedManager
{

    public TurnBasedManager()
    {
    }

    public void enable()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void disable()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightclick(PlayerInteractEvent.RightClickItem event)
    {

    }

    @SubscribeEvent
    public void onAttackCommand(CommandAttackEvent event)
    {
        if (!PokeServerUtils.config.turnbased) return;
        boolean angry = event.getPokemob().getPokemonAIState(IPokemob.ANGRY)
                || ((EntityLiving) event.getPokemob()).getAttackTarget() != null;
        if (!angry) return;
        for (IAIRunnable ai : event.getPokemob().getAIStuff().aiTasks)
        {
            if (ai instanceof AITurnAttack)
            {
                AITurnAttack task = (AITurnAttack) ai;
                if (!task.executingOrders) task.hasOrders = true;
                return;
            }
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onAttackCommand(InitAIEvent event)
    {
        if (!PokeServerUtils.config.turnbased || event.getPokemob().getAIStuff() == null) return;
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
    public void onAttackUse(MoveUse.ActualMoveUse.Init event)
    {
        if (!PokeServerUtils.config.turnbased || !(event.getTarget() instanceof IPokemob)) return;
        if (event.getUser().getPokemonAIState(IMoveConstants.NOITEMUSE))
        {
            System.out.println("deny use");
            event.setCanceled(true);
            event.getUser().setPokemonAIState(IMoveConstants.NOITEMUSE, false);
        }
        for (IAIRunnable ai : event.getUser().getAIStuff().aiTasks)
        {
            if (ai instanceof AITurnAttack)
            {
                AITurnAttack task = (AITurnAttack) ai;
                task.hasOrders = false;
                task.executingOrders = false;
                break;
            }
        }
    }

}
