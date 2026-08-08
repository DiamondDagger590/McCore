package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.external.citizens.CoreCitizensHook;
import com.diamonddagger590.mccore.external.cmi.CoreCMIHook;
import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.modelengine.CoreModelEngineHook;
import com.diamonddagger590.mccore.external.mythicmobs.CoreMythicMobsHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HooksRegistrarTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private PluginManager mockPluginManager;

    private PluginHookRegistry hookRegistry;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("HooksRegistrarTest"));
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    @NotNull
    private BootstrapContext<CorePlugin> context() {
        return new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
    }

    private void enablePlugin(@NotNull String name) {
        when(mockPluginManager.isPluginEnabled(name)).thenReturn(true);
    }

    private void enableAllPlugins() {
        enablePlugin("Nexo");
        enablePlugin("ItemsAdder");
        enablePlugin("HeadDatabase");
        enablePlugin("PlaceholderAPI");
        enablePlugin("ModelEngine");
        enablePlugin("MythicMobs");
        enablePlugin("CMI");
        enablePlugin("Citizens");
    }

    @Test
    @DisplayName("Given no plugins enabled, when registering hooks, then no hooks are registered")
    void register_registersNoHooks_whenNoPluginsEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);

            new HooksRegistrar<CorePlugin>().register(context());

            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_NEXO).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_ITEMS_ADDER).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_PAPI).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_MODEL_ENGINE).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_MYTHIC_MOBS).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_CMI).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_CITIZENS).isPresent());
        }
    }

    @Test
    @DisplayName("Given Nexo enabled, when registering hooks, then Nexo hook is registered")
    void register_registersNexoHook_whenNexoEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("Nexo");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_NEXO).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_NEXO).get() instanceof CoreNexoHook);
        }
    }

    @Test
    @DisplayName("Given ItemsAdder enabled, when registering hooks, then ItemsAdder hook is registered")
    void register_registersItemsAdderHook_whenItemsAdderEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("ItemsAdder");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_ITEMS_ADDER).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_ITEMS_ADDER).get() instanceof CoreItemsAdderHook);
        }
    }

    @Test
    @DisplayName("Given HeadDatabase enabled, when registering hooks, then HeadDatabase hook is registered")
    void register_registersHeadDatabaseHook_whenHeadDatabaseEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("HeadDatabase");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_HEAD_DATABASE).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_HEAD_DATABASE).get() instanceof CoreHeadDatabaseHook);
        }
    }

    @Test
    @DisplayName("Given PlaceholderAPI enabled, when registering hooks, then PAPI hook is registered")
    void register_registersPapiHook_whenPlaceholderApiEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("PlaceholderAPI");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_PAPI).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_PAPI).get() instanceof CorePapiHook);
        }
    }

    @Test
    @DisplayName("Given ModelEngine enabled, when registering hooks, then ModelEngine hook is registered")
    void register_registersModelEngineHook_whenModelEngineEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("ModelEngine");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MODEL_ENGINE).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MODEL_ENGINE).get() instanceof CoreModelEngineHook);
        }
    }

    @Test
    @DisplayName("Given MythicMobs enabled, when registering hooks, then MythicMobs hook is registered")
    void register_registersMythicMobsHook_whenMythicMobsEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("MythicMobs");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MYTHIC_MOBS).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MYTHIC_MOBS).get() instanceof CoreMythicMobsHook);
        }
    }

    @Test
    @DisplayName("Given CMI enabled, when registering hooks, then CMI hook is registered")
    void register_registersCmiHook_whenCmiEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("CMI");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CMI).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CMI).get() instanceof CoreCMIHook);
        }
    }

    @Test
    @DisplayName("Given Citizens enabled, when registering hooks, then Citizens hook is registered")
    void register_registersCitizensHook_whenCitizensEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("Citizens");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CITIZENS).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CITIZENS).get() instanceof CoreCitizensHook);
        }
    }

    @Test
    @DisplayName("Given all testable plugins enabled, when registering hooks, then all 8 hooks are registered")
    void register_registersAllHooks_whenAllPluginsEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enableAllPlugins();

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_NEXO).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_ITEMS_ADDER).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_HEAD_DATABASE).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_PAPI).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MODEL_ENGINE).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_MYTHIC_MOBS).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CMI).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CITIZENS).isPresent());
        }
    }

    @Test
    @DisplayName("Given only a subset of plugins enabled, when registering hooks, then only those hooks are registered")
    void register_registersOnlyEnabledHooks_whenSubsetEnabled() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
            when(mockPluginManager.isPluginEnabled(anyString())).thenReturn(false);
            enablePlugin("Nexo");
            enablePlugin("Citizens");

            new HooksRegistrar<CorePlugin>().register(context());

            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_NEXO).isPresent());
            assertTrue(hookRegistry.pluginHook(CorePluginHookKey.CORE_CITIZENS).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_ITEMS_ADDER).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_PAPI).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_MODEL_ENGINE).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_MYTHIC_MOBS).isPresent());
            assertFalse(hookRegistry.pluginHook(CorePluginHookKey.CORE_CMI).isPresent());
        }
    }
}
