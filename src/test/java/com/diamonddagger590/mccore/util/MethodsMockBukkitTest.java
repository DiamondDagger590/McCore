package com.diamonddagger590.mccore.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodsMockBukkitTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ── getItemType ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getItemType, then returns empty Optional")
    void getItemType_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getItemType(""));
    }

    @Test
    @DisplayName("Given a valid item type key, when calling getItemType, then returns matching ItemType")
    void getItemType_returnsItemType_whenKeyIsValid() {
        Optional<ItemType> result = Methods.getItemType("stone");
        assertTrue(result.isPresent());
        assertEquals(ItemType.STONE, result.get());
    }

    @Test
    @DisplayName("Given an unknown item type key, when calling getItemType, then returns empty Optional")
    void getItemType_returnsEmpty_whenKeyIsUnknown() {
        Optional<ItemType> result = Methods.getItemType("not_a_real_item_type");
        assertFalse(result.isPresent());
    }

    // ── getEnchantment ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getEnchantment, then returns empty Optional")
    void getEnchantment_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getEnchantment(""));
    }

    @Test
    @DisplayName("Given a valid enchantment key, when calling getEnchantment, then returns matching Enchantment")
    void getEnchantment_returnsEnchantment_whenKeyIsValid() {
        Optional<Enchantment> result = Methods.getEnchantment("sharpness");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(Methods.getMinecraftKey("sharpness")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown enchantment key, when calling getEnchantment, then returns empty Optional")
    void getEnchantment_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getEnchantment("not_a_real_enchantment").isPresent());
    }

    // ── getTrimPattern ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getTrimPattern, then returns empty Optional")
    void getTrimPattern_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getTrimPattern(""));
    }

    @Test
    @DisplayName("Given a valid trim pattern key, when calling getTrimPattern, then returns matching TrimPattern")
    void getTrimPattern_returnsTrimPattern_whenKeyIsValid() {
        Optional<TrimPattern> result = Methods.getTrimPattern("sentry");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(Methods.getMinecraftKey("sentry")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown trim pattern key, when calling getTrimPattern, then returns empty Optional")
    void getTrimPattern_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getTrimPattern("not_a_real_pattern").isPresent());
    }

    // ── getTrimMaterial ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getTrimMaterial, then returns empty Optional")
    void getTrimMaterial_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getTrimMaterial(""));
    }

    @Test
    @DisplayName("Given a valid trim material key, when calling getTrimMaterial, then returns matching TrimMaterial")
    void getTrimMaterial_returnsTrimMaterial_whenKeyIsValid() {
        Optional<TrimMaterial> result = Methods.getTrimMaterial("iron");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL).get(Methods.getMinecraftKey("iron")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown trim material key, when calling getTrimMaterial, then returns empty Optional")
    void getTrimMaterial_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getTrimMaterial("not_a_real_material").isPresent());
    }

    // ── getPatternType ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getPatternType, then returns empty Optional")
    void getPatternType_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getPatternType(""));
    }

    @Test
    @DisplayName("Given a valid banner pattern key, when calling getPatternType, then returns matching PatternType")
    void getPatternType_returnsPatternType_whenKeyIsValid() {
        Optional<PatternType> result = Methods.getPatternType("stripe_bottom");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).get(Methods.getMinecraftKey("stripe_bottom")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown banner pattern key, when calling getPatternType, then returns empty Optional")
    void getPatternType_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getPatternType("not_a_real_pattern_type").isPresent());
    }

    // ── getEntityType ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getEntityType, then returns empty Optional")
    void getEntityType_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getEntityType(""));
    }

    @Test
    @DisplayName("Given a valid entity type key, when calling getEntityType, then returns matching EntityType")
    void getEntityType_returnsEntityType_whenKeyIsValid() {
        Optional<EntityType> result = Methods.getEntityType("zombie");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(Methods.getMinecraftKey("zombie")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown entity type key, when calling getEntityType, then returns empty Optional")
    void getEntityType_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getEntityType("not_a_real_entity").isPresent());
    }

    // ── getPotionEffect ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Given an empty string, when calling getPotionEffect, then returns empty Optional")
    void getPotionEffect_returnsEmpty_whenStringIsEmpty() {
        assertEquals(Optional.empty(), Methods.getPotionEffect(""));
    }

    @Test
    @DisplayName("Given a valid potion effect key, when calling getPotionEffect, then returns matching PotionEffectType")
    void getPotionEffect_returnsPotionEffectType_whenKeyIsValid() {
        Optional<PotionEffectType> result = Methods.getPotionEffect("speed");
        assertTrue(result.isPresent());
        assertEquals(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(Methods.getMinecraftKey("speed")),
                result.get()
        );
    }

    @Test
    @DisplayName("Given an unknown potion effect key, when calling getPotionEffect, then returns empty Optional")
    void getPotionEffect_returnsEmpty_whenKeyIsUnknown() {
        assertFalse(Methods.getPotionEffect("not_a_real_effect").isPresent());
    }

    // ── fromBase64 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a valid base64 ItemStack, when calling fromBase64, then returns the deserialized ItemStack")
    void fromBase64_returnsItemStack_whenBase64IsValid() {
        ItemStack original = new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
        String encoded = Base64.getEncoder().encodeToString(original.serializeAsBytes());
        ItemStack result = Methods.fromBase64(encoded);
        assertNotNull(result);
        assertEquals(original, result);
    }

    // ── Case insensitivity ─────────────────────────────────────────────────

    @Test
    @DisplayName("Given an uppercase item type key, when calling getItemType, then returns matching ItemType via case-insensitive lookup")
    void getItemType_returnsItemType_whenKeyIsUpperCase() {
        Optional<ItemType> result = Methods.getItemType("STONE");
        assertTrue(result.isPresent());
        assertEquals(ItemType.STONE, result.get());
    }

    @Test
    @DisplayName("Given a mixed-case item type key, when calling getItemType, then returns matching ItemType via case-insensitive lookup")
    void getItemType_returnsItemType_whenKeyIsMixedCase() {
        Optional<ItemType> result = Methods.getItemType("Stone");
        assertTrue(result.isPresent());
        assertEquals(ItemType.STONE, result.get());
    }
}
