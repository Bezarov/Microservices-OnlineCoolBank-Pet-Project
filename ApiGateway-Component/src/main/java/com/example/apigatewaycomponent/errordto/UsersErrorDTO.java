package com.example.apigatewaycomponent.errordto;

public class UsersErrorDTO {
    private String message;
    private int status;
    private String correlationId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "UsersErrorDTO{" +
                "message='" + message + '\'' +
                ", status=" + status +
                '}';
    }
}
