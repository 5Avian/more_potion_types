package fiveavian.more_potion_types.plugin;

import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import fiveavian.more_potion_types.MorePotionTypes;

public class MorePotionTypesREIPlugin implements REIServerPlugin {
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        EntryComparator<ItemStack> potionComparator = (ctx, stack) -> PotionUtil.getPotionEffects(stack).hashCode();
        if (MorePotionTypes.smallPotion != null)
            registry.register(potionComparator, MorePotionTypes.smallPotion);
        if (MorePotionTypes.bigPotion != null)
            registry.register(potionComparator, MorePotionTypes.bigPotion);
    }
}
