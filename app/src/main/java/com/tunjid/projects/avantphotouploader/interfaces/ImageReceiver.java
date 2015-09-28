package com.tunjid.projects.avantphotouploader.interfaces;

/**
 * Interface for components who request for an image
 */
public interface ImageReceiver {
    void onImagePicked(String imagePath);

}
