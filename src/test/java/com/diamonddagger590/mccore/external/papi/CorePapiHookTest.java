package com.diamonddagger590.mccore.external.papi;

import com.diamonddagger590.mccore.CorePlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CorePapiHookTest {

    @Mock
    private CorePlugin mockPlugin;

    @Nested
    @DisplayName("translateMessage")
    class TranslateMessage {

        @Test
        @DisplayName("Given player and message with placeholders, when translating, then returns resolved string")
        void translateMessage_returnsResolvedString_whenPlaceholdersPresent() {
            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                OfflinePlayer mockPlayer = mock(OfflinePlayer.class);
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(eq(mockPlayer), eq("Hello %player_name%")))
                    .thenReturn("Hello Steve");

                CorePapiHook hook = new CorePapiHook(mockPlugin);
                String result = hook.translateMessage(mockPlayer, "Hello %player_name%");
                assertEquals("Hello Steve", result);
            }
        }

        @Test
        @DisplayName("Given player and message without placeholders, when translating, then returns original message")
        void translateMessage_returnsOriginalMessage_whenNoPlaceholders() {
            try (MockedStatic<PlaceholderAPI> papiStatic = mockStatic(PlaceholderAPI.class)) {
                OfflinePlayer mockPlayer = mock(OfflinePlayer.class);
                papiStatic.when(() -> PlaceholderAPI.setPlaceholders(eq(mockPlayer), eq("Hello World")))
                    .thenReturn("Hello World");

                CorePapiHook hook = new CorePapiHook(mockPlugin);
                String result = hook.translateMessage(mockPlayer, "Hello World");
                assertEquals("Hello World", result);
            }
        }
    }

    @Nested
    @DisplayName("getTagResolver")
    class GetTagResolver {

        @Test
        @DisplayName("Given player, when getting tag resolver, then returns non-null TagResolver")
        void getTagResolver_returnsNonNull_whenPlayerProvided() {
            CorePapiHook hook = new CorePapiHook(mockPlugin);
            Player mockPlayer = mock(Player.class);
            TagResolver resolver = hook.getTagResolver(mockPlayer);
            assertNotNull(resolver);
        }

        @Test
        @DisplayName("Given different players, when getting tag resolver, then returns distinct resolver instances")
        void getTagResolver_returnsDistinctResolver_forDifferentPlayers() {
            CorePapiHook hook = new CorePapiHook(mockPlugin);
            Player player1 = mock(Player.class);
            Player player2 = mock(Player.class);
            TagResolver resolver1 = hook.getTagResolver(player1);
            TagResolver resolver2 = hook.getTagResolver(player2);
            assertNotNull(resolver1);
            assertNotNull(resolver2);
            assertNotSame(resolver1, resolver2);
        }
    }
}
