package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.external.citizens.CoreCitizensHook;
import com.diamonddagger590.mccore.external.cmi.CoreCMIHook;
import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.modelengine.CoreModelEngineHook;
import com.diamonddagger590.mccore.external.mythicmobs.CoreMythicMobsHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PluginHookKeyConstantsTest {

    @Test
    @DisplayName("Given CORE_HEAD_DATABASE key, when hookClass called, then returns CoreHeadDatabaseHook class")
    void coreHeadDatabase_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_HEAD_DATABASE);
        assertEquals(CoreHeadDatabaseHook.class, CorePluginHookKey.CORE_HEAD_DATABASE.hookClass());
    }

    @Test
    @DisplayName("Given CORE_NEXO key, when hookClass called, then returns CoreNexoHook class")
    void coreNexo_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_NEXO);
        assertEquals(CoreNexoHook.class, CorePluginHookKey.CORE_NEXO.hookClass());
    }

    @Test
    @DisplayName("Given CORE_ITEMS_ADDER key, when hookClass called, then returns CoreItemsAdderHook class")
    void coreItemsAdder_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_ITEMS_ADDER);
        assertEquals(CoreItemsAdderHook.class, CorePluginHookKey.CORE_ITEMS_ADDER.hookClass());
    }

    @Test
    @DisplayName("Given CORE_MODEL_ENGINE key, when hookClass called, then returns CoreModelEngineHook class")
    void coreModelEngine_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_MODEL_ENGINE);
        assertEquals(CoreModelEngineHook.class, CorePluginHookKey.CORE_MODEL_ENGINE.hookClass());
    }

    @Test
    @DisplayName("Given CORE_PAPI key, when hookClass called, then returns CorePapiHook class")
    void corePapi_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_PAPI);
        assertEquals(CorePapiHook.class, CorePluginHookKey.CORE_PAPI.hookClass());
    }

    @Test
    @DisplayName("Given CORE_MYTHIC_MOBS key, when hookClass called, then returns CoreMythicMobsHook class")
    void coreMythicMobs_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_MYTHIC_MOBS);
        assertEquals(CoreMythicMobsHook.class, CorePluginHookKey.CORE_MYTHIC_MOBS.hookClass());
    }

    @Test
    @DisplayName("Given CORE_CMI key, when hookClass called, then returns CoreCMIHook class")
    void coreCmi_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_CMI);
        assertEquals(CoreCMIHook.class, CorePluginHookKey.CORE_CMI.hookClass());
    }

    @Test
    @DisplayName("Given CORE_CITIZENS key, when hookClass called, then returns CoreCitizensHook class")
    void coreCitizens_returnsCorrectClass() {
        assertNotNull(CorePluginHookKey.CORE_CITIZENS);
        assertEquals(CoreCitizensHook.class, CorePluginHookKey.CORE_CITIZENS.hookClass());
    }

    // --- PluginHookKeyImpl factory ---

    @Test
    @DisplayName("Given PluginHookKeyImpl.create, when called with a class, then returned key holds that class")
    void pluginHookKeyImpl_create_holdsProvidedClass() {
        PluginHookKey<CorePapiHook> key = PluginHookKeyImpl.create(CorePapiHook.class);
        assertNotNull(key);
        assertEquals(CorePapiHook.class, key.hookClass());
    }

    @Test
    @DisplayName("Given two PluginHookKeyImpl instances for the same class, when comparing, then they are equal via record equality")
    void pluginHookKeyImpl_equalityBasedOnClass() {
        PluginHookKey<CorePapiHook> key1 = PluginHookKeyImpl.create(CorePapiHook.class);
        PluginHookKey<CorePapiHook> key2 = PluginHookKeyImpl.create(CorePapiHook.class);
        assertEquals(key1, key2);
    }
}
