package com.shenkar.aroundme;

public interface ApplicationCallback<T> {
    void done(T result, Exception e);
}
