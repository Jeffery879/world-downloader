package game.data.chunk.palette;

import se.llbit.nbt.SpecificTag;

/**
 * Interface for data type of palettes.
 */
public interface State {
    SpecificTag toNbt();

    /**
     * Since 26.3 block palette NBT uses id/properties instead of Name/Properties.
     */
    default SpecificTag toNbt(int dataVersion) {
        return toNbt();
    }
}
