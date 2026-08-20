package game.data.chunk.version;

import game.data.chunk.Chunk;
import game.data.chunk.palette.Palette;
import packets.builder.PacketBuilder;
import se.llbit.nbt.Tag;

/**
 * Starting from 26.1, each chunk section header holds two shorts instead of one: the non-air block count followed by
 * the fluid count.
 */
public class ChunkSection_26_1 extends ChunkSection_1_21_5 {
    public ChunkSection_26_1(byte y, Palette palette, Chunk chunk) {
        super(y, palette, chunk);
    }

    public ChunkSection_26_1(int sectionY, Tag nbt, Chunk chunk) {
        super(sectionY, nbt, chunk);
    }

    @Override
    protected void writeSectionHeaderExtras(PacketBuilder packet) {
        packet.writeShort(fluidCount);
    }
}