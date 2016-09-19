package pokecube.serverutils.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import pokecube.core.ai.thread.IAIRunnable;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.interfaces.IPokemob;

public class AITurnAttack extends AIAttack
{
    public boolean hasOrders = false;
    final IPokemob pokemob;

    public AITurnAttack(EntityLiving par1EntityLiving)
    {
        super(par1EntityLiving);
        pokemob = (IPokemob) par1EntityLiving;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        EntityLivingBase target = attacker.getAttackTarget();
        AITurnAttack task = null;
        if (target instanceof IPokemob && ((IPokemob) target).getAIStuff() != null)
        {
            for (IAIRunnable ai : ((IPokemob) target).getAIStuff().aiTasks)
            {
                if (ai instanceof AITurnAttack)
                {
                    task = (AITurnAttack) ai;
                    break;
                }
            }
            if (task != null && !pokemob.isPlayerOwned()) hasOrders = task.hasOrders;
            if (task != null && (!hasOrders || !task.hasOrders))
            {
                delayTime = 20;
            }
            else if (hasOrders && task.hasOrders)
            {
                delayTime = 0;
                task.delayTime = 0;
            }
        }
        super.doMainThreadTick(world);
    }

}
