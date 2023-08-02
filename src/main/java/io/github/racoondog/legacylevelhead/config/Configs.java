package io.github.racoondog.legacylevelhead.config;

import dev.xpple.betterconfig.api.Config;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.text.WordUtils;

public class Configs {
    @Config
    public static boolean showSelf = true;

    @Config
    public static String type = "LEVEL";

    @Config(comment = "Enables paid Sk1er LevelHead cosmetics.")
    public static boolean useSk1erCosmetics = false;

    @Config(getter = @Config.Getter("getHeaderColor"))
    public static ColorFormatting headerColor = ColorFormatting.AQUA;
    public static String getHeaderColor() {
        return headerColor.toString() + headerColor.name() + Formatting.RESET;
    }

    @Config(getter = @Config.Getter("getHeaderString"))
    public static String headerString = WordUtils.capitalizeFully(type);
    public static String getHeaderString() {
        return headerColor.toString() + headerString + Formatting.RESET;
    }

    @Config(getter = @Config.Getter("getLevelColor"))
    public static ColorFormatting levelColor = ColorFormatting.YELLOW;
    public static String getLevelColor() {
        return levelColor.toString() + levelColor.name() + Formatting.RESET;
    }

    @Config
    public static boolean enableNametag = true;

    @Config
    public static float fontSize = 1.0f;

    @Config
    public static double offset = 0.0d;

    @Config
    public static int renderDistance = 64;

    @Config
    public static boolean renderBackground = true;
}
