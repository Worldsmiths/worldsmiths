package io.github.worldsmiths.spike.runtime;

public interface ComponentFactory<T> {
    T create();
}
