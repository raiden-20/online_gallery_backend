package ru.vsu.cs.sheina.online_gallery_backend.configuration.minio;

public enum MinioBucket {
    PICTURE("picture");

    private String name;

    MinioBucket(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}