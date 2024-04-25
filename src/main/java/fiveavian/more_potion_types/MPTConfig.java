package fiveavian.more_potion_types;

public class MPTConfig {
    public PotionConfig smallPotion;
    public PotionConfig bigPotion;

    public MPTConfig(PotionConfig smallPotion, PotionConfig bigPotion) {
        this.smallPotion = smallPotion;
        this.bigPotion = bigPotion;
    }

    public static class PotionConfig {
        public boolean isEnabled;
        public float durationMultiplier;
        public boolean shatterAfterUsage;

        public PotionConfig(boolean isEnabled, float durationMultiplier, boolean shatterAfterUsage) {
            this.isEnabled = isEnabled;
            this.durationMultiplier = durationMultiplier;
            this.shatterAfterUsage = shatterAfterUsage;
        }
    }
}
