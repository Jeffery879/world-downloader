package game.data.chunk.version;

import config.Config;
import config.Version;
import game.data.WorldManager;
import game.data.chunk.ChunkSection;
import game.data.chunk.palette.BlockColors;
import game.data.chunk.palette.BlockRegistry;
import game.data.chunk.palette.GlobalPaletteProvider;
import game.data.chunk.palette.Palette;
import game.data.coordinates.CoordinateDim2D;
import game.data.dimension.BiomeRegistry;
import game.data.dimension.Dimension;
import game.data.dimension.DimensionRegistry;
import game.data.registries.RegistryManager;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import packets.DataTypeProvider;
import packets.builder.PacketBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the 1.21.5 (heightmaps array + no length prefix on section data) and 26.1 (fluid count in the section header)
 * chunk wire formats.
 */
class ChunkFormatTest {
    CoordinateDim2D pos = new CoordinateDim2D(0, 0, Dimension.OVERWORLD);

    private Map<Integer, BlockRegistry> palettes;
    private boolean hadPalette;
    private BlockRegistry previousRegistry;

    @AfterEach
    void tearDown() throws Exception {
        Chunk_1_17.setWorldHeight(0, 256);

        // restore the previously registered palette for the default data version
        if (hadPalette) {
            palettes.put(0, previousRegistry);
        } else {
            palettes.remove(0);
        }
    }

    private void setUp() throws Exception {
        WorldManager mock = mock(WorldManager.class);
        when(mock.getBlockColors()).thenReturn(mock(BlockColors.class));
        when(mock.getChunkFactory()).thenReturn(null);

        DimensionRegistry codec = mock(DimensionRegistry.class);
        when(codec.getBiomeRegistry()).thenReturn(mock(BiomeRegistry.class));
        when(mock.getDimensionRegistry()).thenReturn(codec);
        WorldManager.setInstance(mock);

        RegistryManager.setInstance(mock(RegistryManager.class));

        Config.setInstance(new Config());

        // palette constructors look up the global palette for the *default* data version. Register a mock there so
        // that the test does not trigger the report generation / download logic.
        Field field = GlobalPaletteProvider.class.getDeclaredField("palettes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, BlockRegistry> palettes = (Map<Integer, BlockRegistry>) field.get(null);
        if (palettes == null) {
            palettes = new HashMap<>();
            field.set(null, palettes);
        }
        this.palettes = palettes;
        this.hadPalette = palettes.containsKey(0);
        this.previousRegistry = palettes.get(0);
        palettes.put(0, mock(BlockRegistry.class));
    }

    private long[] blocksOf(ChunkSection section) throws Exception {
        Field field = ChunkSection.class.getDeclaredField("blocks");
        field.setAccessible(true);
        return (long[]) field.get(section);
    }

    @Test
    void heightmapsRoundTrip() throws Exception {
        setUp();
        Chunk_1_21_5 chunk = new TestChunk_1_21_5(pos);

        PacketBuilder input = new PacketBuilder();
        input.writeVarInt(2);
        input.writeVarInt(1);
        input.writeVarInt(1);
        input.writeLong(0x0000000100000001L);
        input.writeVarInt(4);
        input.writeVarInt(1);
        input.writeLong(0x0000000200000002L);

        chunk.parseHeightMaps(new DataTypeProvider(input.toArray()));

        assertThat(chunk.heightMap.get("WORLD_SURFACE").isError()).isFalse();
        assertThat(chunk.heightMap.get("MOTION_BLOCKING").isError()).isFalse();
        assertThat(chunk.heightMap.get("OCEAN_FLOOR").isError()).isTrue();
        assertThat(chunk.heightMap.get("WORLD_SURFACE").longArray()).containsExactly(0x0000000100000001L);

        PacketBuilder output = new PacketBuilder();
        chunk.writeHeightMaps(output);

        assertThat(output.toArray()).isEqualTo(input.toArray());
    }

    @Test
    void sectionNoLengthPrefixFrom1_21_5() throws Exception {
        setUp();
        Chunk_1_17.setWorldHeight(-64, 384);
        TestChunk_1_21_5 chunk = new TestChunk_1_21_5(pos);

        PacketBuilder data = sectionBytes(false);

        chunk.readChunkColumn(new DataTypeProvider(data.toArray()));

        ChunkSection_1_21_5 section = (ChunkSection_1_21_5) chunk.section(-4);
        assertThat(section).isNotNull();
        assertThat(section.blockCount).isEqualTo(100);
        assertThat(blocksOf(section)).hasSize(256);
        assertThat(section.biomes).hasSize(1);

        assertThat(chunk.sections().toArray()).isEqualTo(data.toArray());
    }

    @Test
    void sectionFluidCountFrom26_1() throws Exception {
        setUp();
        Chunk_1_17.setWorldHeight(-64, 384);
        TestChunk_26_1 chunk = new TestChunk_26_1(pos);

        PacketBuilder data = sectionBytes(true);

        chunk.readChunkColumn(new DataTypeProvider(data.toArray()));

        ChunkSection_26_1 section = (ChunkSection_26_1) chunk.section(-4);
        assertThat(section).isNotNull();
        assertThat(section.blockCount).isEqualTo(100);
        assertThat(section.fluidCount).isEqualTo(7);
        assertThat(blocksOf(section)).hasSize(256);
        assertThat(section.biomes).hasSize(1);

        assertThat(chunk.sections().toArray()).isEqualTo(data.toArray());
    }

    private PacketBuilder sectionBytes(boolean withFluidCount) {
        PacketBuilder data = new PacketBuilder();
        data.writeShort(100);
        if (withFluidCount) {
            data.writeShort(7);
        }

        // block palette: 4 bits, 1 entry { 7 }
        data.writeByte((byte) 4);
        data.writeVarInt(1);
        data.writeVarInt(7);

        long[] blockData = new long[256];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = i * 7L;
        }
        data.writeLongArray(blockData);

        // biome palette: 1 bit, 1 entry { 0 }
        data.writeByte((byte) 1);
        data.writeVarInt(1);
        data.writeVarInt(0);

        data.writeLong(0xCCFFCCFFCCFFCCFFL);

        return data;
    }

    abstract static class TestChunkBase extends Chunk_1_21_5 {
        TestChunkBase(CoordinateDim2D location, int version) {
            super(location, version);
        }

        ChunkSection section(int y) {
            return getChunkSection(y);
        }

        PacketBuilder sections() {
            return writeSectionData();
        }
    }

    static class TestChunk_1_21_5 extends TestChunkBase {
        TestChunk_1_21_5(CoordinateDim2D location) {
            super(location, Version.V1_21_5.dataVersion);
        }

        @Override
        protected boolean containsBlockEntities(Palette p) {
            return false;
        }
    }

    static class TestChunk_26_1 extends Chunk_26_1 {
        TestChunk_26_1(CoordinateDim2D location) {
            super(location, Version.V26_1.dataVersion);
        }

        @Override
        protected boolean containsBlockEntities(Palette p) {
            return false;
        }

        ChunkSection section(int y) {
            return getChunkSection(y);
        }

        PacketBuilder sections() {
            return writeSectionData();
        }
    }
}