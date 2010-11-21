package com.ejisto.core.classloading.util;
import java.util.ArrayList;

public class AutoGrowingList<T> extends ArrayList<T> {
    @Override
    public T get(int index) {
        ensureCapacity(index+1);
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        ensureCapacity(index+1);
        return super.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        ensureCapacity(index+1);
        super.add(index, element);
    }
}
