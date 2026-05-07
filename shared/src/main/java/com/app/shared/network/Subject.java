package com.app.shared.network;

public interface Subject<T> {
    void addObserver(T observer);    // "Subscribe"
    void removeObserver(T observer); // "Unsubscribe"
}