package game.data.chunk.version;

import game.data.chunk.ChunkSection;
import game.data.chunk.palette.Palette;
import game.data.chunk.palette.PaletteType;
import game.data.coordinates.CoordinateDim2D;
import java.util.ArrayList;
import java.util.List;
import packets.DataTypeProvider;
import packets.builder.PacketBuilder;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.LongArrayTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.Tag;

/**
 * In 1.21.5 the heightmaps were moved out of the NBT tag and into a prefixed array of {@code (type, data)} pairs, and
 * the block states / biomes data arrays lost their length prefix (the length is calculated from the bits per entry).
 */
public class Chunk_1_21_5 extends Chunk_1_20 {
    private static final String[] HEIGHTMAP_NAMES = {
            "WORLD_SURFACE_WG", "WORLD_SURFACE", "OCEAN_FLOOR_WG",
            "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"
    };

    private final List<HeightMapEntry> heightMapEntries = new ArrayList<>();

    public Chunk_1_21_5(CoordinateDim2D location, int version) {
        super(location, version);
    }

    @Override
    protected void parseHeightMaps(DataTypeProvider dataProvider) {
        heightMapEntries.clear();

        CompoundTag heightMap = new CompoundTag();
        int count = dataProvider.readVarInt();
        for (int i = 0; i < count; i++) {
            int type = dataProvider.readVarInt();
            int length = dataProvider.readVarInt();
            long[] data = dataProvider.readLongArray(length);

            heightMapEntries.add(new HeightMapEntry(type, data));
            heightMap.add(getHeightMapName(type), new LongArrayTag(data));
        }
        this.heightMap = heightMap;
    }

    @Override
    protected void parseHeightMaps(Tag tag) {
        heightMapEntries.clear();
        this.heightMap = tag.asCompound().get("Heightmaps").asCompound();

        if (this.heightMap instanceof CompoundTag heightmaps) {
            for (int type = 0; type < HEIGHTMAP_NAMES.length; type++) {
                Tag dataTag = heightmaps.get(HEIGHTMAP_NAMES[type]);
                if (dataTag.isError()) {
                    continue;
                }
                heightMapEntries.add(new HeightMapEntry(type, dataTag.longArray()));
            }
        }
    }

    @Override
    protected void writeHeightMaps(PacketBuilder packet) {
        packet.writeVarInt(heightMapEntries.size());
        for (HeightMapEntry entry : heightMapEntries) {
            packet.writeVarInt(entry.type);
            packet.writeVarInt(entry.data.length);
            packet.writeLongArray(entry.data);
        }
    }

    private String getHeightMapName(int type) {
        return HEIGHTMAP_NAMES[Math.min(Math.max(type, 0), HEIGHTMAP_NAMES.length - 1)];
    }

    @Override
    public ChunkSection createNewChunkSection(byte y, Palette palette) {
        return new ChunkSection_1_21_5(y, palette, this);
    }

    @Override
    protected ChunkSection parseSection(int sectionY, SpecificTag section) {
        return new ChunkSection_1_21_5(sectionY, section, this);
    }

    @Override
    public void readChunkColumn(DataTypeProvider dataProvider) {
        for (int sectionY = getMinBlockSection(); sectionY <= getMaxBlockSection() && dataProvider.hasNext(); sectionY++) {
            ChunkSection_1_21_5 section = (ChunkSection_1_21_5) getChunkSection(sectionY);

            int blockCount = dataProvider.readShort();
            int extra = readChunkSectionExtra(dataProvider);

            Palette blockPalette = Palette.readPalette(dataProvider, PaletteType.BLOCKS);

            if (section == null) {
                section = (ChunkSection_1_21_5) createNewChunkSection((byte) (sectionY & 0xFF), blockPalette);
            } else {
                section.setBlockPalette(blockPalette);
            }

            section.setBlockCount(blockCount);
            applyChunkSectionExtra(section, extra);
            section.setBlocks(dataProvider.readLongArray(ChunkSection_1_16.longsRequired(blockPalette.getBitsPerBlock())));

            Palette biomePalette = Palette.readPalette(dataProvider, PaletteType.BIOMES);
            section.setBiomePalette(biomePalette);
            section.setBiomes(dataProvider.readLongArray(ChunkSection_1_16.longsRequiredBiomes(biomePalette.getBitsPerBlock())));

            setChunkSection(sectionY, section);

            // servers don't (always?) include containers in the list of block_entities. We need to know that these block
            // entities exist, otherwise we'll end up not writing block entity data for them
            if (containsBlockEntities(blockPalette)) {
                findBlockEntities(section, sectionY);
            }
        }
    }

    /**
     * Read any additional fields of the chunk section header that were introduced after 1.21.5.
     */
    protected int readChunkSectionExtra(DataTypeProvider provider) {
        return 0;
    }

    protected void applyChunkSectionExtra(ChunkSection_1_21_5 section, int extra) {
    }

    private static class HeightMapEntry {
        private final int type;
        private final long[] data;

        HeightMapEntry(int type, long[] data) {
            this.type = type;
            this.data = data;
        }
    }
}