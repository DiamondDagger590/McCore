package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class Methods {

    public static boolean isInt(@NotNull String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Component getProgressBar(double progress, int barMultiplier) {
        if (progress < 0.0 || progress > 1.0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
        }
        int barCount = 10 * barMultiplier;
        int greenSegments = (int) (progress * barCount);
        int redSegments = (barCount - greenSegments);
        double remainder = progress % barCount;

        MiniMessage miniMessage = CorePlugin.getInstance().getMiniMessage();
        TextComponent.Builder builder = Component.text();
        builder.append(miniMessage.deserialize("<green>" + "|".repeat(Math.max(0, greenSegments)) + "</green>"));
        if (remainder != 0 && remainder != 1) {
            if (remainder <= 0.25) {
                builder.append(miniMessage.deserialize("<color:#c9ff29>|</color>"));
            } else if (remainder <= 0.50) {
                builder.append(miniMessage.deserialize("<color:#ffcb21>|</color>"));
            } else if (remainder <= 0.75) {
                builder.append(miniMessage.deserialize("<color:#ff822e>|</color>"));
            } else {
                builder.append(miniMessage.deserialize("<color:#ff6417>|</color>"));
            }
        }
        builder.append(miniMessage.deserialize("<color:#ff1418>" + "|".repeat(Math.max(0, redSegments)) + "</color>"));
        return builder.build();
    }
}
