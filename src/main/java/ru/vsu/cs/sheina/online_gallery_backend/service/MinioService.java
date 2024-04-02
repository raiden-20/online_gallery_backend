package ru.vsu.cs.sheina.online_gallery_backend.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    public void deleteFile() {

    }

    public void saveFile() {

    }
}
