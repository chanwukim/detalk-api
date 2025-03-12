package net.detalk.api.support.image;

public class ImageUploadException extends RuntimeException {
    public ImageUploadException() {
        super("Failed to create upload URL");
    }
}
