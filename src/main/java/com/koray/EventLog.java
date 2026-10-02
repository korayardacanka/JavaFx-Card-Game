package com.koray;

import java.util.ArrayList;
import java.util.List;

/** Stores event messages separately while exposing a single-line UI form. */
public final class EventLog {

    private final List<String> messages = new ArrayList<>();

    public void set(String message) {
        messages.clear();
        if (message != null && !message.isEmpty()) {
            messages.add(message);
        }
    }

    public void append(String message) {
        if (message != null && !message.isEmpty()) {
            messages.add(message);
        }
    }

    public void clear() {
        messages.clear();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public String toDisplayString() {
        return String.join("  ", messages);
    }

    public List<String> messages() {
        return List.copyOf(messages);
    }
}
