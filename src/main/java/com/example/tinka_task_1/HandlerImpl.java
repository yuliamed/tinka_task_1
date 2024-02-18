package com.example.tinka_task_1;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class HandlerImpl implements Handler{
    private final Client client;

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) throws ExecutionException {
        long startTime = System.currentTimeMillis();
        int retriesCount = 0;

        while (System.currentTimeMillis() - startTime < 15000) {
            try {
                CompletableFuture<Response> response1Future = CompletableFuture.supplyAsync(() ->
                        client.getApplicationStatus1(id));
                CompletableFuture<Response> response2Future = CompletableFuture.supplyAsync(() ->
                        client.getApplicationStatus2(id));

                CompletableFuture<Response> combinedFuture = response1Future.thenCombine(response2Future, (response1, response2) -> {
                    if (response1 instanceof Response.Success) {
                        return response1;
                    } else if (response2 instanceof Response.Success) {
                        return response2;
                    } else {
                        return response1;
                    }
                });

                Response combinedResponse = combinedFuture.get();

                if (combinedResponse instanceof Response.Success) {
                    Response.Success successResponse = (Response.Success) combinedResponse;
                    return new ApplicationStatusResponse.Success(successResponse.applicationId(), successResponse.applicationStatus());
                } else if (combinedResponse instanceof Response.RetryAfter) {
                    Response.RetryAfter retryAfterResponse = (Response.RetryAfter) combinedResponse;
                    Thread.sleep(retryAfterResponse.delay().toMillis());
                } else if (combinedResponse instanceof Response.Failure) {
                    retriesCount++;
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }

        }
        return new ApplicationStatusResponse.Failure(Duration.ofMillis(System.currentTimeMillis() - startTime), retriesCount);
    }
}
