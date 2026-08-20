package game.data.dimension;

import config.Config;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that 26.1+ worlds store dimension data in the dimensions subfolder while older versions keep
 * using the legacy folder layout.
 */
class DimensionPathTest {

    @Test
    void usesDimensionSubfolderFrom26_1() {
        Config config = new Config();
        Config.setInstance(config);
        Config.setProtocolVersion(776); // 26.2

        assertThat(Dimension.OVERWORLD.getPath()).isEqualTo("dimensions/minecraft/overworld");
        assertThat(Dimension.NETHER.getPath()).isEqualTo("dimensions/minecraft/the_nether");
        assertThat(Dimension.END.getPath()).isEqualTo("dimensions/minecraft/the_end");
    }

    @Test
    void usesLegacyPathsBefore26_1() {
        Config config = new Config();
        Config.setInstance(config);
        Config.setProtocolVersion(770); // 1.21.5

        assertThat(Dimension.OVERWORLD.getPath()).isEqualTo("");
        assertThat(Dimension.NETHER.getPath()).isEqualTo("DIM-1");
        assertThat(Dimension.END.getPath()).isEqualTo("DIM1");
    }
}