package org.axonframework.lifecycle;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for components that can be started and shut down as part of the Application lifecycle.
 *
 * @author Allard Buijze
 * @since 4.6
 */
public interface Lifecycle {

    /**
     * Registers the activities to be executed in the various phases of an application's lifecycle. This could either
     * be at startup, shutdown, or both.
     *
     * @param lifecycle the lifecycle instance to register the handlers with
     *
     * @see LifecycleRegistry#onShutdown(int, Runnable)
     * @see LifecycleRegistry#onShutdown(int, LifecycleHandler)
     * @see LifecycleRegistry#onStart(int, Runnable)
     * @see LifecycleRegistry#onStart(int, LifecycleHandler)
     */
    void registerLifecycleHandlers(LifecycleRegistry lifecycle);

    /**
     * Interface towards the registry that holds all lifecycle handlers for components.
     * <p>
     * Component may register activities to be executed at startup or shutdown. These activities may be executed
     * <ul>
     *     <li>synchronously - see {@link #onStart(int, Runnable)} and {@link  #onShutdown(int, Runnable)}. With this
     *     approach, the lifecycle action is considered completed when the method returns.</li>
     *     <li>asynchronously - see {@link #onStart(int, LifecycleHandler)} and
     *     {@link  #onShutdown(int, LifecycleHandler)}. This approach expects handler methods to return a
     *     {@link CompletableFuture} that completes when the lifecycle action is completed</li>
     * </ul>
     */
    interface LifecycleRegistry {

        /**
         * Registers the given {@code action} to run during the given {@code phase} during startup. Lower {@code phase}s
         * are executed before higher {@code phase}s.
         *
         * @param phase  The phase in which to execute this action
         * @param action The action to perform
         *
         * @see Phase
         */
        default void onStart(int phase, Runnable action) {
            onStart(phase, () -> {
                try {
                    action.run();
                    return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    cf.completeExceptionally(e);
                    return cf;
                }
            });
        }

        /**
         * Registers the given {@code action} to run during the given {@code phase} during shutdown. Higher {@code
         * phase}s are executed before lower {@code phase}s.
         *
         * @param phase  The phase in which to execute this action
         * @param action The action to perform
         *
         * @see Phase
         */
        default void onShutdown(int phase, Runnable action) {
            onShutdown(phase, () -> {
                try {
                    action.run();
                    return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    cf.completeExceptionally(e);
                    return cf;
                }
            });
        }

        /**
         * Registers the given {@code action} to run during the given {@code phase} during startup. Lower {@code phase}s
         * are executed before higher {@code phase}s.
         * <p>
         * Various handlers for the same phase may be executed concurrently, but handlers for subsequent phases will
         * only be invoked when the returned {@link CompletableFuture} completes (normally or exceptionally).
         *
         * @param phase  The phase in which to execute this action
         * @param action The action to perform
         *
         * @see Phase
         */
        void onStart(int phase, LifecycleHandler action);

        /**
         * Registers the given {@code action} to run during the given {@code phase} during shutdown. Higher
         * {@code phase}s are executed before lower {@code phase}s.
         * <p>
         * Various handlers for the same phase may be executed concurrently, but handlers for subsequent phases will
         * only be invoked when the returned {@link CompletableFuture} completes (normally or exceptionally).
         *
         * @param phase  The phase in which to execute this action
         * @param action The action to perform
         *
         * @see Phase
         */
        void onShutdown(int phase, LifecycleHandler action);

    }

    /**
     * Functional interface for lifecycle activities that may run asynchronously
     */
    @FunctionalInterface
    interface LifecycleHandler {

        /**
         * Execute the lifecycle activity.
         * <p>
         * Implementations preferably do not throw exceptions, but return a {@link CompletableFuture} with an
         * exceptional result instead.
         *
         * @return a CompletableFuture that completes when the activity completes.
         */
        CompletableFuture<Void> run();
    }
}
