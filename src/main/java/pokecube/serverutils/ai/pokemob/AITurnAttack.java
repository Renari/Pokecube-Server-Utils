package pokecube.serverutils.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import pokecube.core.ai.thread.aiRunnables.AIAttack;

public class AITurnAttack extends AIAttack
{
    public boolean hasOrders = false;

    public AITurnAttack(EntityLiving par1EntityLiving)
    {
        super(par1EntityLiving);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        if (!hasOrders && delayTime < 10)
        {
            delayTime = 10;
        }
        super.doMainThreadTick(world);
    }

}
