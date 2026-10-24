package game.data.chunk.version;

import game.data.coordinates.CoordinateDim2D;
import packets.DataTypeProvider;
import packets.builder.PacketBuilder;

import java.util.BitSet;

/**
 * Since 26.3 light masks are length-prefixed byte arrays instead of long arrays.
 */
public class Chunk_26_3 extends Chunk_26_1 {
    public Chunk_26_3(CoordinateDim2D location, int version) {
        super(location, version);
    }

    @Override
    protected BitSet readLightMask(DataTypeProvider provider) {
        return provider.readBitSetAsBytes();
    }

    @Override
    protected void writeLightMask(PacketBuilder packet, BitSet mask) {
        packet.writeBitSetAsBytes(mask);
    }
}
