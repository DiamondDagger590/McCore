package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Methods {

    private static final String LOCATION_DELIMITER = ";";

    public static boolean isInt(@NotNull String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Component getProgressBar(double progress, int barCount) {
        if (progress < 0.0 || progress > 1.0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
        }
        int greenSegments = (int) (progress * barCount);
        int redSegments = (barCount - greenSegments);
        double remainder = progress % barCount;

        MiniMessage miniMessage = CorePlugin.getInstance().getMiniMessage();
        TextComponent.Builder builder = Component.text();
        builder.append(miniMessage.deserialize("<green>" + "|".repeat(Math.max(0, greenSegments)) + "</green>"));
        if (remainder > 0.0f && remainder < 1.0f) {
            if (remainder <= 0.25f) {
                builder.append(miniMessage.deserialize("<color:#c9ff29>|</color>"));
            } else if (remainder <= 0.50f) {
                builder.append(miniMessage.deserialize("<color:#ffcb21>|</color>"));
            } else if (remainder <= 0.75f) {
                builder.append(miniMessage.deserialize("<color:#ff822e>|</color>"));
            } else {
                builder.append(miniMessage.deserialize("<color:#ff6417>|</color>"));
            }
            // Remove a red segment since we are doing fun colors
            redSegments--;
        }
        builder.append(miniMessage.deserialize("<color:#ff1418>" + "|".repeat(Math.max(0, redSegments)) + "</color>"));
        return builder.build();
    }

    public static String serializeLocation(@NotNull Location location) {
        return location.getX() + LOCATION_DELIMITER + location.getY() + LOCATION_DELIMITER + location.getZ() + LOCATION_DELIMITER + location.getWorld().getUID();
    }

    public static Optional<Location> deserializeLocation(@NotNull String serializedLocation) {
        String[] values = serializedLocation.split(LOCATION_DELIMITER);
        if (values.length != 4) {
            throw new IllegalArgumentException("Expected a serialized location following the format of x;y;z;uuid, instead got " + serializedLocation);
        }
        int x = Integer.parseInt(values[0]);
        int y = Integer.parseInt(values[1]);
        int z = Integer.parseInt(values[2]);
        World world = Bukkit.getWorld(values[3]);
        return world == null ? Optional.empty() : Optional.of(new Location(world, x, y, z));
    }
}
