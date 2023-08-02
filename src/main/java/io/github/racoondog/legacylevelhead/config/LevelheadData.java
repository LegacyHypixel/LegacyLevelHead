package io.github.racoondog.legacylevelhead.config;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;

public class LevelheadData {
    public final int level;
    @Nullable public final ColorFormatting headerColor;
    @Nullable public final String header;
    @Nullable public final ColorFormatting levelColor;

    public LevelheadData(JsonObject object) {
        level = Integer.parseInt(object.get("value").getAsString());
        headerColor = object.has("headerColor") ? ColorFormatting.fromIntColor(object.get("headerColor").getAsInt()) : null;
        header = object.has("headerString") ? object.get("headerString").getAsString() : null;
        levelColor = object.has("footerColor") ? ColorFormatting.fromIntColor(object.get("footerColor").getAsInt()) : null;

        if (headerColor != null || header != null || levelColor != null) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("HeaderColor: " + String.valueOf(headerColor) + "; Header: " + String.valueOf(header) + "; LevelColor: " + String.valueOf(levelColor)));
        }
    }


    public String formatNametag() {
        return getHeaderColor() + getHeaderString() + ": " + getLevelColor() + level;
    }

    public ColorFormatting getHeaderColor() {
        if (Configs.useSk1erCosmetics) {
            return headerColor != null ? headerColor : Configs.headerColor;
        } else return Configs.headerColor;
    }

    public String getHeaderString() {
        if (Configs.useSk1erCosmetics) {
            return header != null ? header : Configs.headerString;
        } else return Configs.headerString;
    }

    public ColorFormatting getLevelColor() {
        if (Configs.useSk1erCosmetics) {
            return levelColor != null ? levelColor : Configs.levelColor;
        } else return Configs.levelColor;
    }
}
