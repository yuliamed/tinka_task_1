package com.example.tinka_task_1;

import java.time.Duration;

public sealed interface Response {
    record Success(String applicationStatus, String applicationId) implements Response {}
    record RetryAfter(Duration delay) implements Response {}
    record Failure(Throwable ex) implements Response {}
}