package ru.sfedu.model;

import java.util.Objects;

public class Result<T> {

    public Result(){}

    public Result(String message, int code, T result) {
        this.message = message;
        this.code = code;
        this.result = result;
    }

    private String message;

    private int code;

    private T result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result<?> result1 = (Result<?>) o;
        return code == result1.code && Objects.equals(message, result1.message) && Objects.equals(result, result1.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, code, result);
    }

    @Override
    public String toString() {
        return "Result{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", result=" + result +
                '}';
    }
}
