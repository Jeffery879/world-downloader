package game.data;

import config.Config;
import game.data.dimension.Dimension;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.LongTag;
import se.llbit.nbt.Tag;
import util.NbtUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that 26.1+ worlds get the world gen settings written to the registry data file
 * (data/minecraft/world_gen_settings.dat) that the game requires when loading the world.
 */
class LevelDataTest {
    @TempDir
    Path tempDir;

    @Test
    void writesWorldGenSettingsFileFrom26_1() throws Exception {
        Config config = new Config();
        setField(config, "worldOutputDir", tempDir.toString());
        Config.setInstance(config);

        // pretend we're connected to a 26.2 server
        Config.setProtocolVersion(776);
        setField(config, "dataVersion", 4903);
        setField(config, "gameVersion", "26.2");

        WorldManager world = mock(WorldManager.class);
        WorldManager.setInstance(world);

        LevelData levelData = new LevelData(world);
        levelData.save();

        Path file = tempDir.resolve("data").resolve("minecraft").resolve("world_gen_settings.dat");
        assertThat(file).exists();

        Tag parsed;
        try (java.io.InputStream in = Files.newInputStream(file)) {
            parsed = NbtUtil.read(in);
        }
        CompoundTag root = (CompoundTag) parsed.unpack();
        CompoundTag data = root.get("data").asCompound();

        // field renamed from generate_features to generate_structures in the 26.x registry data format
        assertThat(data.get("generate_structures").isError()).isFalse();
        assertThat(data.get("generate_features").isError()).isTrue();
        assertThat(data.get("dimensions").asCompound().get("minecraft:overworld").isError()).isFalse();
        assertThat(data.get("seed").longValue()).isEqualTo(0);

        assertThat(((IntTag) root.get("DataVersion")).value).isEqualTo(4903);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}