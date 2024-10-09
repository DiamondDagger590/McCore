package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

/**
 * A static helper class containing common methods shared across multiple plugins.
 */
public class Methods {

    private static final String LOCATION_DELIMITER = ";";

    /**
     * Checks to see if the provided string is an integer.
     *
     * @param string The string to check
     * @return {@code true} if the provided string is an integer.
     */
    public static boolean isInt(@NotNull String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates a colored progress bar based on the provided double. Each bar represents a different
     * amount of percentage (basically 100/barCount). Each bar then gets colored to represent how much of that bar
     * has been "filled" by the provided progression. The color goes from red -> green as there is more and more progress.
     * <p>
     * The bar count will specify how many bars should be used for displaying the progress.
     * The more bars provided, the higher the degree of accuracy when it comes to coloring each
     * individual bar.
     *
     * @param progress The amount of progress to create a bar for. Must be between 0.0 and 1.0
     * @param barCount The amount of bars to have in the progress bar.
     * @return A {@link Component} representing a progress bar.
     * @throws IllegalArgumentException If the provided progress is not between 0.0 and 1.0
     */
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

    /**
     * Serializes the provided {@link Location} into a string.
     *
     * @param location The {@link Location} to serialize.
     * @return A serialized string representation of the provided {@link Location}.
     */
    public static String serializeLocation(@NotNull Location location) {
        return location.getX() + LOCATION_DELIMITER + location.getY() + LOCATION_DELIMITER + location.getZ() + LOCATION_DELIMITER + location.getWorld().getUID();
    }

    /**
     * Deserializes the provided string into a {@link Location}.
     * <p>
     * Serialized locations must follow the format of x;y;z;world_uuid.
     *
     * @param serializedLocation The serialized location to be deserialized.
     * @return An {@link Optional} that contains the deserialized {@link Location} or empty if the world uuid was invalid.
     * @throws IllegalArgumentException if the serialized location does not have 4 values separated by ;.
     */
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

    /**
     * Creates a new {@link Location} that a {@link org.bukkit.entity.Player} can be teleported to in order
     * to look at a target location.
     *
     * @param originLocation The origin {@link Location} that needs to look at the target location.
     * @param lookAt         The {@link Location} to orient the origin to look at.
     * @return A {@link Location} that is pointed towards the target location.
     */
    public static Location lookAt(Location originLocation, Location lookAt) {
        //Clone the loc to prevent applied changes to the input loc
        originLocation = originLocation.clone();

        // Values of change in distance (make it relative)
        double dx = lookAt.getX() - originLocation.getX();
        double dy = lookAt.getY() - originLocation.getY();
        double dz = lookAt.getZ() - originLocation.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                originLocation.setYaw((float) (1.5 * Math.PI));
            } else {
                originLocation.setYaw((float) (0.5 * Math.PI));
            }
            originLocation.setYaw((float) originLocation.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            originLocation.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        originLocation.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        originLocation.setYaw(-originLocation.getYaw() * 180f / (float) Math.PI);
        originLocation.setPitch(originLocation.getPitch() * 180f / (float) Math.PI);

        return originLocation;
    }

    /**
     * Turns the provided string into a {@link Duration}. The string can be any combination of an integer following by a character denoting
     * the time unit.
     * <p>
     * An example would be `15s5m1h` to represent a duration of 1 hour, 5 minutes and 15 seconds.
     * <p>
     * Accepted time units are:
     * <ul>
     *     <li>s - second</li>
     *     <li>m - minute</li>
     *     <li>h - hour</li>
     *     <li>d - day</li>
     *     <li>w - week</li>
     *     <li>y - year</li>
     * </ul>
     *
     * @param timeString The time string to parse.
     * @return A {@link Duration} representation of the provided string.
     */
    @NotNull
    public static Duration getTimeInSeconds(@NotNull String timeString) {
        Duration duration = Duration.ZERO;
        StringBuilder numberBuilder = new StringBuilder();

        for (char c : timeString.toCharArray()) {
            if (Character.isDigit(c)) {
                numberBuilder.append(c);
            } else {
                long value = Long.parseLong(numberBuilder.toString());
                numberBuilder.setLength(0);

                duration = switch (c) {
                    case 's' -> duration.plusSeconds(value);
                    case 'm' -> duration.plusMinutes(value);
                    case 'h' -> duration.plusHours(value);
                    case 'd' -> duration.plusDays(value);
                    case 'w' -> duration.plusDays(value * 7);
                    case 'y' -> duration.plusDays(value * 365);
                    default -> throw new IllegalArgumentException("Invalid time unit: " + c);
                };
            }
        }
        return duration;
    }
}
