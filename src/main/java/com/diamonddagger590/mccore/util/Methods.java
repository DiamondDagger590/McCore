package com.diamonddagger590.mccore.util;

import org.jetbrains.annotations.NotNull;

public class Methods {

    public static boolean isInt(@NotNull String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
