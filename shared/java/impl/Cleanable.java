package io.github.humbleui.skija.impl;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Cleanable {

    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    private static final ConcurrentHashMap<PhantomReference<?>, Runnable> cleanupActions = new ConcurrentHashMap<>();
    private static final ExecutorService cleanerExecutor = Executors.newSingleThreadExecutor();

    static {
        cleanerExecutor.execute(() -> {
            try {
                while (true) {
                    PhantomReference<?> ref = (PhantomReference<?>) referenceQueue.remove();
                    Runnable action = cleanupActions.remove(ref);
                    if (action != null) {
                        action.run();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static Cleanable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj);
        Objects.requireNonNull(action);
        PhantomReference<Object> ref = new PhantomReference<>(obj, referenceQueue);
        cleanupActions.put(ref, action);
        return new Cleanable(ref);
    }

    private final PhantomReference<?> reference;

    private Cleanable(PhantomReference<?> reference) {
        this.reference = reference;
    }

    public void clean() {
        Runnable action = cleanupActions.remove(reference);
        if (action != null) {
            action.run();
        }
        reference.clear();
    }
}