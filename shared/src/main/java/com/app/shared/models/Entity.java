package com.app.shared.models;

import java.io.Serializable;
import java.util.UUID;

public abstract class Entity implements Serializable {
    protected String id;

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

}