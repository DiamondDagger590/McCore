package com.diamonddagger590.mccore.database.response;

import com.diamonddagger590.mccore.testing.ManagedExecutorExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetItemRequestTest {

    @RegisterExtension
    ManagedExecutorExtension executorExtension = new ManagedExecutorExtension();

    @Test
    @DisplayName("Given a null future, when request is created, then throws NullPointerException")
    void constructor_throwsNullPointerException_whenFutureIsNull() {
        assertThrows(NullPointerException.class, () -> new GetItemRequest<String>(null));
    }

    @Test
    @DisplayName("Given a pending future, when created, then state is PENDING_RESPONSE")
    void constructor_setsPendingState_whenFutureNotComplete() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        assertEquals(GetItemResponseState.PENDING_RESPONSE, request.getItemResponseState());
        assertTrue(request.getItem().isEmpty());
    }

    @Test
    @DisplayName("Given a future that completes with a value, when completed, then state is ITEM_FOUND and item is present")
    void state_becomesItemFound_whenFutureCompletesWithValue() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        future.complete("test-value");

        assertEquals(GetItemResponseState.ITEM_FOUND, request.getItemResponseState());
        assertTrue(request.getItem().isPresent());
        assertEquals("test-value", request.getItem().get());
    }

    @Test
    @DisplayName("Given a future that completes with null, when completed, then state is ITEM_NOT_FOUND")
    void state_becomesItemNotFound_whenFutureCompletesWithNull() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        future.complete(null);

        assertEquals(GetItemResponseState.ITEM_NOT_FOUND, request.getItemResponseState());
        assertTrue(request.getItem().isEmpty());
    }

    @Test
    @DisplayName("Given a future that completes exceptionally, when completed, then state is ERRORED")
    void state_becomesErrored_whenFutureCompletesExceptionally() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        future.completeExceptionally(new RuntimeException("db error"));

        assertEquals(GetItemResponseState.ERRORED, request.getItemResponseState());
        assertTrue(request.getItem().isEmpty());
    }

    @Test
    @DisplayName("Given a request, when getItemCompletableFuture is called, then the original future is returned")
    void getItemCompletableFuture_returnsOriginalFuture() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        assertSame(future, request.getItemCompletableFuture());
    }

    @Test
    @DisplayName("Given a pre-completed future with a value, when request is created, then state is immediately ITEM_FOUND")
    void constructor_setsItemFound_whenFutureAlreadyCompletedWithValue() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(42);
        GetItemRequest<Integer> request = new GetItemRequest<>(future);

        assertEquals(GetItemResponseState.ITEM_FOUND, request.getItemResponseState());
        assertTrue(request.getItem().isPresent());
        assertEquals(42, request.getItem().get());
    }

    @Test
    @DisplayName("Given a pre-completed future with null, when request is created, then state is immediately ITEM_NOT_FOUND")
    void constructor_setsItemNotFound_whenFutureAlreadyCompletedWithNull() {
        CompletableFuture<String> future = CompletableFuture.completedFuture(null);
        GetItemRequest<String> request = new GetItemRequest<>(future);

        assertEquals(GetItemResponseState.ITEM_NOT_FOUND, request.getItemResponseState());
        assertTrue(request.getItem().isEmpty());
    }

    @Test
    @DisplayName("Given a pre-failed future, when request is created, then state is immediately ERRORED")
    void constructor_setsErrored_whenFutureAlreadyFailed() {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("fail"));
        GetItemRequest<String> request = new GetItemRequest<>(future);

        assertEquals(GetItemResponseState.ERRORED, request.getItemResponseState());
        assertTrue(request.getItem().isEmpty());
    }

    @Test
    @DisplayName("Given a future completing with a complex object, when completed, then item holds the object")
    void state_becomesItemFound_whenFutureCompletesWithComplexObject() {
        record TestItem(int id, String name) {}
        CompletableFuture<TestItem> future = new CompletableFuture<>();
        GetItemRequest<TestItem> request = new GetItemRequest<>(future);

        TestItem item = new TestItem(1, "sword");
        future.complete(item);

        assertEquals(GetItemResponseState.ITEM_FOUND, request.getItemResponseState());
        assertTrue(request.getItem().isPresent());
        assertSame(item, request.getItem().get());
    }

    @Test
    @DisplayName("Given item is empty before completion, when getItem is called, then returns empty Optional")
    void getItem_returnsEmpty_beforeCompletion() {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetItemRequest<String> request = new GetItemRequest<>(future);

        assertFalse(request.getItem().isPresent());
    }

    @Test
    @DisplayName("Given a future completed from another thread, when polling state, then state and item are visible")
    void getItemResponseState_isVisible_whenFutureCompletedFromAnotherThread() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            CompletableFuture<String> future = new CompletableFuture<>();
            GetItemRequest<String> request = new GetItemRequest<>(future);

            CountDownLatch latch = new CountDownLatch(1);
            executorExtension.getExecutor().submit(() -> {
                future.complete("cross-thread-value");
                latch.countDown();
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));

            assertEquals(GetItemResponseState.ITEM_FOUND, request.getItemResponseState());
            assertTrue(request.getItem().isPresent());
            assertEquals("cross-thread-value", request.getItem().get());
        }
    }

    @Test
    @DisplayName("Given a future completed with null from another thread, when polling state, then state reflects ITEM_NOT_FOUND")
    void getItemResponseState_reflectsNotFound_whenNullCompletedFromAnotherThread() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            CompletableFuture<String> future = new CompletableFuture<>();
            GetItemRequest<String> request = new GetItemRequest<>(future);

            CountDownLatch latch = new CountDownLatch(1);
            executorExtension.getExecutor().submit(() -> {
                future.complete(null);
                latch.countDown();
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));

            assertEquals(GetItemResponseState.ITEM_NOT_FOUND, request.getItemResponseState());
            assertTrue(request.getItem().isEmpty());
        }
    }

    @Test
    @DisplayName("Given a future failed from another thread, when polling state, then state reflects ERRORED")
    void getItemResponseState_reflectsErrored_whenExceptionFromAnotherThread() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            CompletableFuture<String> future = new CompletableFuture<>();
            GetItemRequest<String> request = new GetItemRequest<>(future);

            CountDownLatch latch = new CountDownLatch(1);
            executorExtension.getExecutor().submit(() -> {
                future.completeExceptionally(new RuntimeException("async failure"));
                latch.countDown();
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));

            assertEquals(GetItemResponseState.ERRORED, request.getItemResponseState());
            assertTrue(request.getItem().isEmpty());
        }
    }
}
