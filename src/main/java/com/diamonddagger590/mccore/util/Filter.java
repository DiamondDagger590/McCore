package com.diamonddagger590.mccore.util;

import java.util.Collection;

public interface Filter<E> {

    public Collection<E> filter(Collection<E> list);
}
