package com.diamonddagger590.mccore.external.papi;

import com.diamonddagger590.mccore.CorePlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CorePapiHookTest {

    private CorePapiHook hook;

    @BeforeEach
    void setUp() throws Exception {
        Unsafe unsafe = getUnsafe();
        hook = (CorePapiHook) unsafe.allocateInstance(CorePapiHook.class);
    }

    private static Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    @Nested
    @DisplayName("translateMessage(OfflinePlayer, String)")
    class TranslateMessage {

        @Test
        @DisplayName("Given a player and a message with placeholders, when translating, then delegates to PlaceholderAPI")
        void translateMessage_delegatesToPlaceholderApi() {
            OfflinePlayer player = mock(OfflinePlayer.class);

            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(player, "Hello %player_name%!"))
                    .thenReturn("Hello Steve!");

                String result = hook.translateMessage(player, "Hello %player_name%!");

                assertEquals("Hello Steve!", result);
            }
        }

        @Test
        @DisplayName("Given a message with no placeholders, when translating, then returns the message unchanged")
        void translateMessage_returnsUnchanged_whenNoPlaceholders() {
            OfflinePlayer player = mock(OfflinePlayer.class);

            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(player, "No placeholders here"))
                    .thenReturn("No placeholders here");

                String result = hook.translateMessage(player, "No placeholders here");

                assertEquals("No placeholders here", result);
            }
        }

        @Test
        @DisplayName("Given an empty message, when translating, then returns empty string")
        void translateMessage_returnsEmpty_whenMessageIsEmpty() {
            OfflinePlayer player = mock(OfflinePlayer.class);

            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(player, ""))
                    .thenReturn("");

                String result = hook.translateMessage(player, "");

                assertEquals("", result);
            }
        }

        @Test
        @DisplayName("Given multiple placeholders, when translating, then all are resolved")
        void translateMessage_resolvesMultiplePlaceholders() {
            OfflinePlayer player = mock(OfflinePlayer.class);
            String input = "%player_name% has %player_health% HP";
            String expected = "Steve has 20.0 HP";

            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(player, input))
                    .thenReturn(expected);

                String result = hook.translateMessage(player, input);

                assertEquals(expected, result);
            }
        }
    }

    @Nested
    @DisplayName("getTagResolver(Player)")
    class GetTagResolver {

        @Test
        @DisplayName("Given a player, when getting tag resolver, then returns a non-null TagResolver")
        void getTagResolver_returnsNonNullResolver() {
            Player player = mock(Player.class);

            TagResolver resolver = hook.getTagResolver(player);

            assertNotNull(resolver);
        }
    }
}
