package fiveavian.more_potion_types.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PotionContainerItem extends Item {
    public Item potionItem;

    public PotionContainerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }
        var pos = hitResult.getBlockPos();
        if (!world.canPlayerModifyAt(user, pos) || !world.getFluidState(pos).isIn(FluidTags.WATER)) {
            return TypedActionResult.pass(stack);
        }
        world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        world.emitGameEvent(user, GameEvent.FLUID_PICKUP, pos);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        var result = ItemUsage.exchangeStack(stack, user, PotionUtil.setPotion(new ItemStack(potionItem), Potions.WATER));
        return TypedActionResult.success(result, world.isClient);
    }
}
