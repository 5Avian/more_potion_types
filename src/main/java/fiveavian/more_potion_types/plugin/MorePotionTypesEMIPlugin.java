package fiveavian.more_potion_types.plugin;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionUtil;
import fiveavian.more_potion_types.MorePotionTypes;

public class MorePotionTypesEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        Comparison potionComparison = Comparison.of((a, b) -> PotionUtil.getPotionEffects(a.getNbt()).equals(PotionUtil.getPotionEffects(b.getNbt())));
        if (MorePotionTypes.smallPotion != null)
            registry.setDefaultComparison(MorePotionTypes.smallPotion, potionComparison);
        if (MorePotionTypes.bigPotion != null)
            registry.setDefaultComparison(MorePotionTypes.bigPotion, potionComparison);
    }
}
