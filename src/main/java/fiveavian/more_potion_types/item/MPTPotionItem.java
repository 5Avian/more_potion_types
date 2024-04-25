package fiveavian.more_potion_types.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MPTPotionItem extends PotionItem {
    private static final Random RANDOM = new Random();
    private final ParticleEffect shatterParticleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, getDefaultStack());
    public Item containerItem = Items.GLASS_BOTTLE;
    public float durationMultiplier = 1.0f;
    public boolean shatterAfterUsage = false;

    public MPTPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        return PotionUtil.setPotion(super.getDefaultStack(), Potions.WATER);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return (int) (32 * durationMultiplier);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient) {
            return stack;
        }
        var player = (ServerPlayerEntity) user;
        for (var instance : PotionUtil.getPotionEffects(stack)) {
            if (instance.getEffectType().isInstant()) {
                instance.getEffectType().applyInstantEffect(player, player, player, instance.getAmplifier(), 1);
            } else {
                var duration = (int) (instance.getDuration() * durationMultiplier);
                player.addStatusEffect(new StatusEffectInstance(instance.getEffectType(), duration, instance.getAmplifier(), instance.isAmbient(), instance.shouldShowParticles(), instance.shouldShowIcon()));
            }
        }
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
            if (!shatterAfterUsage) {
                if (stack.isEmpty()) {
                    return containerItem.getDefaultStack();
                } else {
                    player.getInventory().insertStack(containerItem.getDefaultStack());
                }
            }
        }
        if (shatterAfterUsage) {
            world.playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL, 0.5f, 1.1f, true);
            for (int i = 0; i < 8; i++) {
                world.addParticle(
                        shatterParticleEffect,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        RANDOM.nextDouble(-0.1, 0.1), RANDOM.nextDouble(0.0, 0.1), RANDOM.nextDouble(-0.1, 0.1)
                );
            }
        }
        Criteria.CONSUME_ITEM.trigger(player, stack);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        player.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        PotionUtil.buildTooltip(stack, tooltip, durationMultiplier);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey(), Text.translatable(PotionUtil.getPotion(stack).finishTranslationKey("item.minecraft.potion.effect.")));
    }
}
