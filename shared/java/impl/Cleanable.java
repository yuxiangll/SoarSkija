package io.github.humbleui.skija.impl;

import java.util.Objects;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class Cleanable {

    private static final CleanerWrapper cleanerWrapper;

    static {
        CleanerWrapper temp = null;
        try {
            temp = new JavaRefCleanerWrapper();
        } catch (Throwable e) {
            try {
                temp = new SunMiscCleanerWrapper();
            } catch (Throwable ex) {
                throw new NoClassDefFoundError("No suitable Cleaner implementation found");
            }
        }
        cleanerWrapper = temp;
    }

    public static Cleanable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj);
        Objects.requireNonNull(action);
        return new Cleanable(cleanerWrapper.register(obj, action));
    }

    private final Object cleanerInstance;

    private Cleanable(Object cleanerInstance) {
        this.cleanerInstance = cleanerInstance;
    }

    public void clean() {
        cleanerWrapper.clean(cleanerInstance);
    }

    private interface CleanerWrapper {
        Object register(Object obj, Runnable action);

        void clean(Object cleanerInstance);
    }

    private static class JavaRefCleanerWrapper implements CleanerWrapper {
        private final Object cleaner;
        private final MethodHandle registerHandle;
        private final MethodHandle cleanHandle;

        public JavaRefCleanerWrapper() throws Exception {

            Class<?> cleanerClass = Class.forName("java.lang.ref.Cleaner");

            MethodHandle createHandle = MethodHandles.publicLookup()
                    .findStatic(cleanerClass, "create", MethodType.methodType(cleanerClass));
            cleaner = createHandle.invoke();

            Class<?> cleanableClass = Class.forName("java.lang.ref.Cleaner$Cleanable");
            registerHandle = MethodHandles.publicLookup()
                    .findVirtual(cleanerClass, "register",
                            MethodType.methodType(cleanableClass, Object.class, Runnable.class));

            cleanHandle = MethodHandles.publicLookup()
                    .findVirtual(cleanableClass, "clean",
                            MethodType.methodType(void.class));
        }

        public Object register(Object obj, Runnable action) {
            try {
                return registerHandle.invoke(cleaner, obj, action);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to register Cleaner", e);
            }
        }

        public void clean(Object cleanerInstance) {
            try {
                cleanHandle.invoke(cleanerInstance);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to clean", e);
            }
        }
    }

    private static class SunMiscCleanerWrapper implements CleanerWrapper {
        private final MethodHandle createCleanerHandle;
        private final MethodHandle cleanHandle;

        public SunMiscCleanerWrapper() throws Exception {
            Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
            createCleanerHandle = MethodHandles.publicLookup().findStatic(cleanerClass, "create",
                    MethodType.methodType(cleanerClass, Object.class, Runnable.class));
            cleanHandle = MethodHandles.publicLookup().findVirtual(cleanerClass, "clean",
                    MethodType.methodType(void.class));
        }

        public Object register(Object obj, Runnable action) {
            try {
                return createCleanerHandle.invokeWithArguments(obj, action);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create Cleaner instance", e);
            }
        }

        public void clean(Object cleanerInstance) {
            try {
                cleanHandle.invokeWithArguments(cleanerInstance);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to clean", e);
            }
        }
    }
}