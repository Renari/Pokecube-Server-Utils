package pokecube.serverutils.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import pokecube.core.ai.thread.IAIRunnable;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.interfaces.IPokemob;

public class AITurnAttack extends AIAttack
{
    public boolean hasOrders       = false;
    public boolean executingOrders = false;
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
        task:
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
            if (task == null) break task;
            if (!pokemob.isPlayerOwned()) hasOrders = task.hasOrders;
            boolean bothOrder = hasOrders && task.hasOrders;

            if (!bothOrder && !executingOrders)
            {
                delayTime = 20;
            }
            else if (hasOrders && task.hasOrders)
            {
                delayTime = 0;
                executingOrders = true;
                hasOrders = false;
                task.delayTime = 0;
                task.executingOrders = true;
                task.hasOrders = false;
            }
        }
        super.doMainThreadTick(world);
    }

}
