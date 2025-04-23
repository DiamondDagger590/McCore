package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Base64;
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
     * @return A {@link String} representing a progress bar.
     * @throws IllegalArgumentException If the provided progress is not between 0.0 and 1.0
     */
    public static String getProgressBarAsString(double progress, int barCount) {
        if (progress < 0.0 || progress > 1.0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
        }
        int greenSegments = (int) (progress * barCount);
        int redSegments = (barCount - greenSegments);
        double remainder = progress % barCount;

        MiniMessage miniMessage = CorePlugin.getInstance().getMiniMessage();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<green>").append("|".repeat(Math.max(0, greenSegments))).append("</green>");
        if (remainder > 0.0f && remainder < 1.0f) {
            if (remainder <= 0.25f) {
                stringBuilder.append("<color:#c9ff29>|</color>");
            } else if (remainder <= 0.50f) {
                stringBuilder.append("<color:#ffcb21>|</color>");
            } else if (remainder <= 0.75f) {
                stringBuilder.append("<color:#ff822e>|</color>");
            } else {
                stringBuilder.append("<color:#ff6417>|</color>");
            }
            // Remove a red segment since we are doing fun colors
            redSegments--;
        }
        stringBuilder.append("<color:#ff1418>").append("|".repeat(Math.max(0, redSegments))).append("</color>");
        return stringBuilder.toString();
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

    /**
     * Gets a {@link ItemType} from the provided string.
     *
     * @param type The string representation of a {@link ItemType}.
     * @return An {@link Optional} containing the {@link ItemType} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<ItemType> getItemType(@NotNull String type) {
        return type.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(getMinecraftKey(type)));
    }

    /**
     * Gets a {@link Enchantment} from the provided string.
     *
     * @param value The string representation of a {@link Enchantment}.
     * @return An {@link Optional} containing the {@link Enchantment} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<Enchantment> getEnchantment(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link ItemFlag} from the provided string.
     *
     * @param name The string representation of a {@link ItemFlag}.
     * @return An {@link Optional} containing the {@link ItemFlag} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<ItemFlag> getFlag(final String name) {
        ItemFlag flag = null;
        for (final ItemFlag value : ItemFlag.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                flag = value;
                break;
            }
        }
        return Optional.ofNullable(flag);
    }

    /**
     * Gets a {@link TrimPattern} from the provided string.
     *
     * @param value The string representation of a {@link TrimPattern}.
     * @return An {@link Optional} containing the {@link TrimPattern} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<TrimPattern> getTrimPattern(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link TrimMaterial} from the provided string.
     *
     * @param value The string representation of a {@link TrimMaterial}.
     * @return An {@link Optional} containing the {@link TrimMaterial} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<TrimMaterial> getTrimMaterial(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link PatternType} from the provided string.
     *
     * @param value The string representation of a {@link PatternType}.
     * @return An {@link Optional} containing the {@link PatternType} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<PatternType> getPatternType(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link EntityType} from the provided string.
     *
     * @param value The string representation of a {@link EntityType}.
     * @return An {@link Optional} containing the {@link EntityType} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<EntityType> getEntityType(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link PotionEffectType} from the provided string.
     *
     * @param value The string representation of a {@link PotionEffectType}.
     * @return An {@link Optional} containing the {@link PotionEffectType} or it will
     * be empty if no matches.
     */
    @NotNull
    public static Optional<PotionEffectType> getPotionEffect(@NotNull final String value) {
        return value.isEmpty() ? Optional.empty() : Optional.ofNullable(RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(getMinecraftKey(value)));
    }

    /**
     * Gets a {@link Color} from the provided RGB string.
     *
     * @param color A string containing RGB data using {@code ,} as a delimiter.
     * @return An {@link Optional} containing the {@link Color} matching the provided string
     * or empty if no match.
     */
    @NotNull
    public static Optional<Color> getRGB(@NotNull final String color) {
        final String[] rgb = color.split(",");
        if (rgb.length != 3) {
            return Optional.empty();
        }

        int red = Integer.parseInt(rgb[0]);
        int green = Integer.parseInt(rgb[1]);
        int blue = Integer.parseInt(rgb[2]);
        return Optional.of(Color.fromRGB(red, green, blue));
    }

    /**
     * Gets the {@link Color} matching the provided string.
     *
     * @param value The string representation of a {@link Color}.
     * @return The {@link Color} matching the provided string or {@link Color#WHITE}
     */
    @NotNull
    public static Color getColor(@NotNull final String value) {
        return switch (value.toLowerCase()) {
            case "aqua" -> Color.AQUA;
            case "black" -> Color.BLACK;
            case "blue" -> Color.BLUE;
            case "fuchsia" -> Color.FUCHSIA;
            case "gray" -> Color.GRAY;
            case "green" -> Color.GREEN;
            case "lime" -> Color.LIME;
            case "maroon" -> Color.MAROON;
            case "navy" -> Color.NAVY;
            case "olive" -> Color.OLIVE;
            case "orange" -> Color.ORANGE;
            case "purple" -> Color.PURPLE;
            case "red" -> Color.RED;
            case "silver" -> Color.SILVER;
            case "teal" -> Color.TEAL;
            case "yellow" -> Color.YELLOW;
            default -> Color.WHITE;
        };
    }

    /**
     * Gets the {@link DyeColor} matching the provided string.
     *
     * @param value The string representation of a {@link DyeColor}.
     * @return The {@link DyeColor} matching the provided string or {@link DyeColor#WHITE}.
     */
    @NotNull
    public static DyeColor getDyeColor(@NotNull final String value) {
        return switch (value.toLowerCase()) {
            case "orange" -> DyeColor.ORANGE;
            case "magenta", "fuchsia" -> DyeColor.MAGENTA;
            case "light_blue", "aqua" -> DyeColor.LIGHT_BLUE;
            case "yellow" -> DyeColor.YELLOW;
            case "lime" -> DyeColor.LIME;
            case "pink" -> DyeColor.PINK;
            case "gray" -> DyeColor.GRAY;
            case "light_gray", "silver" -> DyeColor.LIGHT_GRAY;
            case "cyan", "teal" -> DyeColor.CYAN;
            case "purple" -> DyeColor.PURPLE;
            case "blue", "navy" -> DyeColor.BLUE;
            case "brown" -> DyeColor.BROWN;
            case "green", "olive" -> DyeColor.GREEN;
            case "red", "maroon" -> DyeColor.RED;
            case "black" -> DyeColor.BLACK;
            default -> DyeColor.WHITE;
        };
    }

    /**
     * Gets the {@link NamespacedKey} from the provided key using the minecraft namespace.
     *
     * @param key The key to use.
     * @return The {@link NamespacedKey} from the provided key using the minecraft namespace.
     */
    @NotNull
    public static NamespacedKey getMinecraftKey(@NotNull String key) {
        return NamespacedKey.minecraft(key);
    }

    /**
     * Creates an {@link ItemStack} from the provided base64 string.
     *
     * @param base64 The base64 string to get an {@link ItemStack} from.
     * @return An {@link ItemStack} from the provided base64 string.
     */
    @NotNull
    public static ItemStack fromBase64(@NotNull String base64) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }
}
