package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.listener.ChatResponseListener;
import com.diamonddagger590.mccore.listener.GuiCloseListener;
import com.diamonddagger590.mccore.listener.GuiRefreshListener;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerRegistrarTest {

    private ListenerRegistrar<CorePlugin> registrar;

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private PluginManager mockPluginManager;

    private MockedStatic<Bukkit> bukkitStatic;

    @BeforeEach
    void setUp() {
        registrar = new ListenerRegistrar<>();
        bukkitStatic = mockStatic(Bukkit.class);
        bukkitStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
    }

    @AfterEach
    void tearDown() {
        bukkitStatic.close();
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then exactly 3 listeners are registered")
    void register_registersThreeListeners() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        registrar.register(context);

        verify(mockPluginManager, times(3)).registerEvents(any(), eq(mockPlugin));
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then a GuiCloseListener is registered")
    void register_registersGuiCloseListener() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        registrar.register(context);

        ArgumentCaptor<org.bukkit.event.Listener> captor = ArgumentCaptor.forClass(org.bukkit.event.Listener.class);
        verify(mockPluginManager, times(3)).registerEvents(captor.capture(), eq(mockPlugin));

        List<org.bukkit.event.Listener> listeners = captor.getAllValues();
        assertTrue(listeners.stream().anyMatch(l -> l instanceof GuiCloseListener),
                "GuiCloseListener should be registered");
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then a GuiRefreshListener is registered")
    void register_registersGuiRefreshListener() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        registrar.register(context);

        ArgumentCaptor<org.bukkit.event.Listener> captor = ArgumentCaptor.forClass(org.bukkit.event.Listener.class);
        verify(mockPluginManager, times(3)).registerEvents(captor.capture(), eq(mockPlugin));

        List<org.bukkit.event.Listener> listeners = captor.getAllValues();
        assertTrue(listeners.stream().anyMatch(l -> l instanceof GuiRefreshListener),
                "GuiRefreshListener should be registered");
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then a ChatResponseListener is registered")
    void register_registersChatResponseListener() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        registrar.register(context);

        ArgumentCaptor<org.bukkit.event.Listener> captor = ArgumentCaptor.forClass(org.bukkit.event.Listener.class);
        verify(mockPluginManager, times(3)).registerEvents(captor.capture(), eq(mockPlugin));

        List<org.bukkit.event.Listener> listeners = captor.getAllValues();
        assertTrue(listeners.stream().anyMatch(l -> l instanceof ChatResponseListener),
                "ChatResponseListener should be registered");
    }

    @Test
    @DisplayName("Given a TEST startup profile, when registering, then all listeners are still registered")
    void register_registersListeners_withTestProfile() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);

        registrar.register(context);

        verify(mockPluginManager, times(3)).registerEvents(any(), eq(mockPlugin));
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then all listeners are registered with the correct plugin")
    void register_usesCorrectPluginInstance() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        registrar.register(context);

        verify(mockPluginManager, times(3)).registerEvents(any(), eq(mockPlugin));
    }
}
