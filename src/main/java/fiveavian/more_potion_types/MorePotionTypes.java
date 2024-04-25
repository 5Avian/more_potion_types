package fiveavian.more_potion_types;

import com.google.gson.GsonBuilder;
import fiveavian.more_potion_types.item.PotionContainerItem;
import fiveavian.more_potion_types.item.MPTPotionItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import java.io.*;

public class MorePotionTypes implements ModInitializer {
    public static final String MOD_ID = "more_potion_types";
    public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
    public static final File CONFIG_FILE = new File("config/" + MOD_ID + ".json");

    public static MPTConfig config = new MPTConfig(
            new MPTConfig.PotionConfig(true, 0.75f, true),
            new MPTConfig.PotionConfig(true, 1.5f, false)
    );

    public static PotionContainerItem glassVial = null;
    public static MPTPotionItem smallPotion = null;
    public static PotionContainerItem glassJar = null;
    public static MPTPotionItem bigPotion = null;

    @Override
    public void onInitialize() {
        try {
            loadConfig();
            registerContent();
            LOGGER.info("Initialized " + MOD_ID);
        } catch (Exception ex) {
            LOGGER.fatal("Failed to initialize " + MOD_ID, ex);
        }
    }

    private void loadConfig() throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        CONFIG_FILE.getParentFile().mkdirs();
        if (CONFIG_FILE.createNewFile()) {
            LOGGER.info("Creating new config file");
            try (var stream = new FileOutputStream(CONFIG_FILE); var writer = new OutputStreamWriter(stream)) {
                gson.toJson(config, writer);
            }
        } else {
            LOGGER.info("Loading existing config file");
            try (var stream = new FileInputStream(CONFIG_FILE); var reader = new InputStreamReader(stream)) {
                config = gson.fromJson(reader, MPTConfig.class);
            }
        }
    }

    private void registerContent() {
        if (config.smallPotion.isEnabled) {
            glassVial = registerItem("glass_vial", new PotionContainerItem(new Item.Settings().maxCount(64)));
            smallPotion = registerItem("small_potion", new MPTPotionItem(new Item.Settings().maxCount(1)));
            glassVial.potionItem = smallPotion;
            smallPotion.containerItem = glassVial;
            smallPotion.durationMultiplier = config.smallPotion.durationMultiplier;
            smallPotion.shatterAfterUsage = config.smallPotion.shatterAfterUsage;
            BrewingRecipeRegistry.registerPotionType(smallPotion);
            addItemToItemGroup(ItemGroups.INGREDIENTS, glassVial);
            addPotionItemToItemGroup(ItemGroups.FOOD_AND_DRINK, smallPotion);
            ColorProviderRegistry.ITEM.register(this::getPotionColor, smallPotion);
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(smallPotion, createContainerEmptyingBehavior(glassVial, smallPotion));
            CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(smallPotion, createContainerEmptyingBehavior(glassVial, smallPotion));
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(glassVial, createContainerFillingBehavior(glassVial, smallPotion));
        }
        if (config.bigPotion.isEnabled) {
            glassJar = registerItem("glass_jar", new PotionContainerItem(new Item.Settings().maxCount(64)));
            bigPotion = registerItem("big_potion", new MPTPotionItem(new Item.Settings().maxCount(1)));
            glassJar.potionItem = bigPotion;
            bigPotion.containerItem = glassJar;
            bigPotion.durationMultiplier = config.bigPotion.durationMultiplier;
            bigPotion.shatterAfterUsage = config.bigPotion.shatterAfterUsage;
            BrewingRecipeRegistry.registerPotionType(bigPotion);
            addItemToItemGroup(ItemGroups.INGREDIENTS, glassJar);
            addPotionItemToItemGroup(ItemGroups.FOOD_AND_DRINK, bigPotion);
            ColorProviderRegistry.ITEM.register(this::getPotionColor, bigPotion);
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(bigPotion, createContainerEmptyingBehavior(glassJar, bigPotion));
            CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(bigPotion, createContainerEmptyingBehavior(glassJar, bigPotion));
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(glassJar, createContainerFillingBehavior(glassJar, bigPotion));
        }
    }

    private <T extends Item> T registerItem(String path, T item) {
        return Registry.register(Registries.ITEM, new Identifier(MOD_ID, path), item);
    }

    private void addItemToItemGroup(RegistryKey<ItemGroup> itemGroup, Item item) {
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(item));
    }

    private void addPotionItemToItemGroup(RegistryKey<ItemGroup> itemGroup, Item item) {
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> {
            Registries.POTION.getReadOnlyWrapper().streamEntries()
                    .filter(e -> !e.matchesKey(Potions.EMPTY_KEY))
                    .map(e -> PotionUtil.setPotion(new ItemStack(item), e.value()))
                    .forEach(entries::add);
        });
    }

    private CauldronBehavior createContainerFillingBehavior(Item container, Item potion) {
        return (state, world, pos, player, hand, stack) -> {
            if (world.isClient) {
                return ActionResult.CONSUME;
            }
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionUtil.setPotion(potion.getDefaultStack(), Potions.WATER)));
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(container));
            return ActionResult.SUCCESS;
        };
    }

    private CauldronBehavior createContainerEmptyingBehavior(Item container, Item potion) {
        return (state, world, pos, player, hand, stack) -> {
            var level = state.getOrEmpty(LeveledCauldronBlock.LEVEL).orElse(0);
            if (level == 3 || PotionUtil.getPotion(stack) != Potions.WATER) {
                return ActionResult.PASS;
            }
            if (world.isClient) {
                return ActionResult.CONSUME;
            }
            if (level == 0) {
                world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
            } else {
                world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, level + 1));
            }
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, container.getDefaultStack()));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
            world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(potion));
            return ActionResult.SUCCESS;
        };
    }

    private int getPotionColor(ItemStack stack, int tintIndex) {
        return tintIndex > 0 ? -1 : PotionUtil.getColor(stack);
    }
}
