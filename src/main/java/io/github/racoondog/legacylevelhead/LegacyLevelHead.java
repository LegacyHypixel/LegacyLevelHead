package io.github.racoondog.legacylevelhead;

import com.google.gson.Gson;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import io.github.racoondog.legacylevelhead.config.Configs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyLevelHead implements ModInitializer {
    public static final Gson GSON = new Gson();
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;

        new ModConfigBuilder("levelhead", Configs.class).build();
    }
}
