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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorePluginHookKeyTest {

    @Test
    @DisplayName("Given CORE_HEAD_DATABASE key, when checking hook class, then returns CoreHeadDatabaseHook class")
    void coreHeadDatabaseKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_HEAD_DATABASE);
        assertEquals(CoreHeadDatabaseHook.class, CorePluginHookKey.CORE_HEAD_DATABASE.hookClass());
    }

    @Test
    @DisplayName("Given CORE_NEXO key, when checking hook class, then returns CoreNexoHook class")
    void coreNexoKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_NEXO);
        assertEquals(CoreNexoHook.class, CorePluginHookKey.CORE_NEXO.hookClass());
    }

    @Test
    @DisplayName("Given CORE_ITEMS_ADDER key, when checking hook class, then returns CoreItemsAdderHook class")
    void coreItemsAdderKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_ITEMS_ADDER);
        assertEquals(CoreItemsAdderHook.class, CorePluginHookKey.CORE_ITEMS_ADDER.hookClass());
    }

    @Test
    @DisplayName("Given CORE_MODEL_ENGINE key, when checking hook class, then returns CoreModelEngineHook class")
    void coreModelEngineKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_MODEL_ENGINE);
        assertEquals(CoreModelEngineHook.class, CorePluginHookKey.CORE_MODEL_ENGINE.hookClass());
    }

    @Test
    @DisplayName("Given CORE_PAPI key, when checking hook class, then returns CorePapiHook class")
    void corePapiKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_PAPI);
        assertEquals(CorePapiHook.class, CorePluginHookKey.CORE_PAPI.hookClass());
    }

    @Test
    @DisplayName("Given CORE_MYTHIC_MOBS key, when checking hook class, then returns CoreMythicMobsHook class")
    void coreMythicMobsKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_MYTHIC_MOBS);
        assertEquals(CoreMythicMobsHook.class, CorePluginHookKey.CORE_MYTHIC_MOBS.hookClass());
    }

    @Test
    @DisplayName("Given CORE_CMI key, when checking hook class, then returns CoreCMIHook class")
    void coreCmiKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_CMI);
        assertEquals(CoreCMIHook.class, CorePluginHookKey.CORE_CMI.hookClass());
    }

    @Test
    @DisplayName("Given CORE_CITIZENS key, when checking hook class, then returns CoreCitizensHook class")
    void coreCitizensKey_returnsCorrectHookClass() {
        assertNotNull(CorePluginHookKey.CORE_CITIZENS);
        assertEquals(CoreCitizensHook.class, CorePluginHookKey.CORE_CITIZENS.hookClass());
    }

    @Test
    @DisplayName("Given all core plugin hook keys, when comparing, then all keys are distinct")
    void allCorePluginHookKeys_areDistinct() {
        Set<PluginHookKey<?>> keys = Set.of(
                CorePluginHookKey.CORE_HEAD_DATABASE,
                CorePluginHookKey.CORE_NEXO,
                CorePluginHookKey.CORE_ITEMS_ADDER,
                CorePluginHookKey.CORE_MODEL_ENGINE,
                CorePluginHookKey.CORE_PAPI,
                CorePluginHookKey.CORE_MYTHIC_MOBS,
                CorePluginHookKey.CORE_CMI,
                CorePluginHookKey.CORE_CITIZENS
        );
        assertEquals(8, keys.size());
    }
}
