package io.github.humbleui.skija.impl;

import java.lang.ref.Cleaner;
import java.util.Objects;

public final class Cleanable {

    private static final Cleaner cleaner = Cleaner.create();

    public static Cleanable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj);
        Objects.requireNonNull(action);
        return new Cleanable(cleaner.register(obj, action));
    }

    private final Cleaner.Cleanable cleanable;

    private Cleanable(Cleaner.Cleanable cleanable) {
        this.cleanable = cleanable;
    }

    public void clean() {
        cleanable.clean();
    }
}