package game.data.chunk.version;

import game.data.chunk.ChunkSection;
import game.data.chunk.palette.Palette;
import game.data.coordinates.CoordinateDim2D;
import packets.DataTypeProvider;
import se.llbit.nbt.SpecificTag;

/**
 * Since 26.1 every chunk section header carries a fluid count short in addition to the non-air block count.
 */
public class Chunk_26_1 extends Chunk_1_21_5 {
    public Chunk_26_1(CoordinateDim2D location, int version) {
        super(location, version);
    }

    @Override
    protected int readChunkSectionExtra(DataTypeProvider provider) {
        return provider.readShort();
    }

    @Override
    protected void applyChunkSectionExtra(ChunkSection_1_21_5 section, int extra) {
        section.setFluidCount(extra);
    }

    @Override
    public ChunkSection createNewChunkSection(byte y, Palette palette) {
        return new ChunkSection_26_1(y, palette, this);
    }

    @Override
    protected ChunkSection parseSection(int sectionY, SpecificTag section) {
        return new ChunkSection_26_1(sectionY, section, this);
    }
}