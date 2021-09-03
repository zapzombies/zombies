package io.github.zap.zombies.game.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * Wraps data for a particle
 * @param <D> The type of the data wrapped by the class
 */
@AllArgsConstructor
public abstract class ParticleDataWrapper<D> {

    public final static String DUST_DATA_NAME = "dust";
    public final static String BLOCK_DATA_NAME = "block";
    public final static String ITEM_STACK_DATA_NAME = "itemStack";

    private final String type;

    @Getter
    private final D data;

    public static class DustParticleDataWrapper extends ParticleDataWrapper<Particle.DustOptions> {

        public DustParticleDataWrapper(Particle.DustOptions dustOptions) {
            super(DUST_DATA_NAME, dustOptions);
        }

        private DustParticleDataWrapper() {
            super(DUST_DATA_NAME, null);
        }

    }

    public static class BlockParticleDataWrapper extends ParticleDataWrapper<BlockData> {

        public BlockParticleDataWrapper(BlockData blockData) {
            super(BLOCK_DATA_NAME, blockData);
        }

        private BlockParticleDataWrapper() {
            super(BLOCK_DATA_NAME, null);
        }

    }

    public static class ItemStackParticleDataWrapper extends ParticleDataWrapper<ItemStack> {

        public ItemStackParticleDataWrapper(ItemStack itemStack) {
            super(ITEM_STACK_DATA_NAME, itemStack);
        }

        private ItemStackParticleDataWrapper() {
            super(ITEM_STACK_DATA_NAME, null);
        }

    }

}
