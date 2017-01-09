package com.tenxerconsulting.swagger.doclet.model;

import io.swagger.models.Path;

/**
 * Wrapper class around {@link Path} in order to set priorities
 * @author RJ Ewing
 */
public class PathWrapper {

    private Path path;

    private int priority = Integer.MAX_VALUE;
    private String description;

    public PathWrapper(Path path, int priority, String description) {
        this.path = path;
        this.priority = priority;
        this.description = description;
    }

    public Path getPath() {
        return path;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathWrapper)) return false;

        PathWrapper that = (PathWrapper) o;

        if (getPriority() != that.getPriority()) return false;
        if (getPath() != null ? !getPath().equals(that.getPath()) : that.getPath() != null) return false;
        return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;
    }

    @Override
    public int hashCode() {
        int result = getPath() != null ? getPath().hashCode() : 0;
        result = 31 * result + getPriority();
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        return result;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
