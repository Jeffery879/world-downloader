package game.data.chunk.version;

import game.data.chunk.Chunk;
import game.data.chunk.palette.Palette;
import game.data.chunk.palette.SingleValuePalette;
import java.util.Arrays;
import packets.builder.PacketBuilder;
import se.llbit.nbt.Tag;

/**
 * 1.21.5 changed the chunk section format: the length of the block states and biomes data arrays is no longer
 * prefixed to the packet but instead calculated from the bits per entry and the number of entries (4096 for blocks,
 * 64 for biomes).
 */
public class ChunkSection_1_21_5 extends ChunkSection_1_18 {
    protected int fluidCount = 0;

    public ChunkSection_1_21_5(byte y, Palette palette, Chunk chunk) {
        super(y, palette, chunk);
    }

    public ChunkSection_1_21_5(int sectionY, Tag nbt, Chunk chunk) {
        super(sectionY, nbt, chunk);
    }

    @Override
    public void write(PacketBuilder packet) {
        if (blockCount < 0) { blockCount = palette.isEmpty() ? 0 : 4096; }

        packet.writeShort(blockCount);
        writeSectionHeaderExtras(packet);

        palette.write(packet);
        packet.writeLongArray(fitLongArray(blocks, ChunkSection_1_16.longsRequired(palette.getBitsPerBlock())));

        if (biomePalette == null) {
            biomePalette = new SingleValuePalette(0);
            biomePalette.biomePalette();
        }
        biomePalette.write(packet);
        packet.writeLongArray(fitLongArray(biomes, ChunkSection_1_16.longsRequiredBiomes(biomePalette.getBitsPerBlock())));
    }

    /**
     * Hook to write any additional section header fields introduced after 1.21.5.
     */
    protected void writeSectionHeaderExtras(PacketBuilder packet) {
    }

    public void setFluidCount(int fluidCount) {
        this.fluidCount = fluidCount;
    }

    private long[] fitLongArray(long[] data, int expected) {
        if (data == null) {
            return new long[expected];
        }
        if (data.length == expected) {
            return data;
        }
        return Arrays.copyOf(data, expected);
    }
}