package com.diamonddagger590.mccore.exception.builder.item;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class InvalidItemBuilderExceptionTest {

    @Test
    @DisplayName("Given a builder and message, when constructing, then getBuilder returns the builder")
    void getBuilder_returnsSameBuilder_whenConstructed() {
        BaseItemBuilder<?> builder = mock(BaseItemBuilder.class);
        InvalidItemBuilderException ex = new InvalidItemBuilderException(builder, "test message");
        assertEquals(builder, ex.getBuilder());
    }

    @Test
    @DisplayName("Given a builder and message, when constructing, then getMessage returns the message")
    void getMessage_returnsProvidedMessage_whenConstructed() {
        BaseItemBuilder<?> builder = mock(BaseItemBuilder.class);
        InvalidItemBuilderException ex = new InvalidItemBuilderException(builder, "ItemStack type mismatch");
        assertEquals("ItemStack type mismatch", ex.getMessage());
    }

    @Test
    @DisplayName("Given a builder and message, when checking type, then it is a RuntimeException")
    void invalidItemBuilderException_isRuntimeException_always() {
        BaseItemBuilder<?> builder = mock(BaseItemBuilder.class);
        assertInstanceOf(RuntimeException.class, new InvalidItemBuilderException(builder, "error"));
    }

    @Test
    @DisplayName("Given a builder and message, when constructing, then builder is non-null")
    void getBuilder_returnsNonNull_whenConstructedWithMock() {
        BaseItemBuilder<?> builder = mock(BaseItemBuilder.class);
        InvalidItemBuilderException ex = new InvalidItemBuilderException(builder, "test");
        assertNotNull(ex.getBuilder());
    }
}
