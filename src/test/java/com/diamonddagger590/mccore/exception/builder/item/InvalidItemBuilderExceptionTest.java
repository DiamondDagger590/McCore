package com.diamonddagger590.mccore.exception.builder.item;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class InvalidItemBuilderExceptionTest {

    @Mock
    private BaseItemBuilder<?> mockBuilder;

    @Test
    @DisplayName("Given a builder and message, when constructing, then message is stored")
    void getMessage_returnsMessage_whenConstructed() {
        InvalidItemBuilderException ex = new InvalidItemBuilderException(mockBuilder, "Not a firework");
        assertEquals("Not a firework", ex.getMessage());
    }

    @Test
    @DisplayName("Given a builder and message, when constructing, then builder is accessible")
    void getBuilder_returnsBuilder_whenConstructed() {
        InvalidItemBuilderException ex = new InvalidItemBuilderException(mockBuilder, "Invalid type");
        assertSame(mockBuilder, ex.getBuilder());
    }

    @Test
    @DisplayName("Given an InvalidItemBuilderException, when checking type, then it is a RuntimeException")
    void invalidItemBuilderException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new InvalidItemBuilderException(mockBuilder, "test"));
    }

    @Test
    @DisplayName("Given an empty message, when constructing, then empty message is stored")
    void getMessage_returnsEmpty_whenConstructedWithEmptyMessage() {
        InvalidItemBuilderException ex = new InvalidItemBuilderException(mockBuilder, "");
        assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("Given different builders, when constructing two exceptions, then each returns its own builder")
    void getBuilder_returnsDifferentBuilders_whenConstructedSeparately(@Mock BaseItemBuilder<?> otherBuilder) {
        InvalidItemBuilderException ex1 = new InvalidItemBuilderException(mockBuilder, "first");
        InvalidItemBuilderException ex2 = new InvalidItemBuilderException(otherBuilder, "second");
        assertSame(mockBuilder, ex1.getBuilder());
        assertSame(otherBuilder, ex2.getBuilder());
    }
}
