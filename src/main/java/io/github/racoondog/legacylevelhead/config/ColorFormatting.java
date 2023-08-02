package io.github.racoondog.legacylevelhead.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.awt.*;

/**
 * We can't exclude enum values from the command argument, so we wrap {@link Formatting} and only expose the colors.
 */
public enum ColorFormatting {
    BLACK(Formatting.BLACK),
    DARK_BLUE(Formatting.DARK_BLUE),
    DARK_GREEN(Formatting.DARK_GREEN),
    DARK_AQUA(Formatting.DARK_AQUA),
    DARK_RED(Formatting.DARK_RED),
    DARK_PURPLE(Formatting.DARK_PURPLE),
    GOLD(Formatting.GOLD),
    GRAY(Formatting.GRAY),
    DARK_GRAY(Formatting.DARK_GRAY),
    BLUE(Formatting.BLUE),
    GREEN(Formatting.GREEN),
    AQUA(Formatting.AQUA),
    RED(Formatting.RED),
    LIGHT_PURPLE(Formatting.LIGHT_PURPLE),
    YELLOW(Formatting.YELLOW),
    WHITE(Formatting.WHITE);

    public final Formatting formatting;

    ColorFormatting(net.minecraft.util.Formatting formatting) {
        this.formatting = formatting;
    }


    @Override
    public String toString() {
        return formatting.toString();
    }

    /**
     * Get the closest {@link ColorFormatting} to the provided color.
     */
    public static ColorFormatting fromIntColor(int intColor) {
        Color color = new Color(intColor);
        int lowestDistance = Integer.MAX_VALUE;
        int closestIndex = 0;
        for (char i = 0; i < 16; i++) {
            Color fColor = new Color(MinecraftClient.getInstance().textRenderer.getColor(i));
            int distance = Math.abs(color.getRed() - fColor.getRed()) + Math.abs(color.getBlue() - fColor.getBlue()) + Math.abs(color.getBlue() - fColor.getBlue());
            if (distance < lowestDistance) {
                lowestDistance = distance;
                closestIndex = i;
            }
        }
        return ColorFormatting.values()[closestIndex];
    }
}
