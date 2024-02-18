package com.example.tinka_task_1;

import java.util.concurrent.ExecutionException;

public interface Handler {
    ApplicationStatusResponse performOperation(String id) throws ExecutionException;
}
