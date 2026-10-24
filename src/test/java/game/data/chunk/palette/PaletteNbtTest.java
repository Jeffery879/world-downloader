package game.data.chunk.palette;

import config.Version;
import org.junit.jupiter.api.Test;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.StringTag;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Since 26.3 the block palette NBT uses id/properties instead of Name/Properties.
 */
class PaletteNbtTest {

    private BlockState stateWithProperties() {
        CompoundTag properties = new CompoundTag();
        properties.add("axis", new StringTag("y"));
        return new BlockState("minecraft:oak_log", 17, properties);
    }

    @Test
    void writesIdAndPropertiesFor26_3() {
        BlockState state = stateWithProperties();
        CompoundTag nbt = state.toNbt(Version.V26_3.dataVersion).asCompound();

        assertThat(nbt.get("id").stringValue()).isEqualTo("minecraft:oak_log");
        assertThat(nbt.get("properties").asCompound().get("axis").stringValue()).isEqualTo("y");
        assertThat(nbt.get("Name").isError()).isTrue();
        assertThat(nbt.get("Properties").isError()).isTrue();
    }

    @Test
    void writesNameAndPropertiesBefore26_3() {
        BlockState state = stateWithProperties();
        CompoundTag nbt = state.toNbt(Version.V26_2.dataVersion).asCompound();

        assertThat(nbt.get("Name").stringValue()).isEqualTo("minecraft:oak_log");
        assertThat(nbt.get("Properties").asCompound().get("axis").stringValue()).isEqualTo("y");
        assertThat(nbt.get("id").isError()).isTrue();
        assertThat(nbt.get("properties").isError()).isTrue();
    }

    @Test
    void readsBothFieldNames() {
        CompoundTag oldStyle = new CompoundTag();
        oldStyle.add("Name", new StringTag("minecraft:stone"));
        assertThat(Palette.entryName(oldStyle)).isEqualTo("minecraft:stone");

        CompoundTag newStyle = new CompoundTag();
        newStyle.add("id", new StringTag("minecraft:dirt"));
        assertThat(Palette.entryName(newStyle)).isEqualTo("minecraft:dirt");

        assertThat(Palette.entryName(new StringTag("minecraft:air"))).isEqualTo("minecraft:air");
    }
}
