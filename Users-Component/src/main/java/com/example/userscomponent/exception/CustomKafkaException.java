package com.example.userscomponent.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponseException;

public class CustomKafkaException extends ErrorResponseException {
    private final String reason;

    public CustomKafkaException(HttpStatusCode status, @Nullable String reason) {
        super(status);
        this.reason = reason;
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }

    @Override
    @NonNull
    public String getMessage() {
        return getStatusCode() + (this.reason != null ? " \"" + this.reason + "\"" : "");
    }
}
