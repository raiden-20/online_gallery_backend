package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import ru.vsu.cs.sheina.online_gallery_backend.configuration.minio.MinioBucket;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.FileTooBigException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;
    private final Integer FILE_MAX_SIZE = 2 * 1024 * 1024;

    @Value("${minio.host}")
    private String storageHost;

    public void deleteFile(String url) {
        String fileName = getFileName(url);
        minioService.deleteFile(fileName);
    }

    public String saveFile(MultipartFile file, String id) {
        if (!file.isEmpty() && file.getSize() > FILE_MAX_SIZE) {
            throw new FileTooBigException();
        }

        String newUrl = storageHost + "/" + MinioBucket.PICTURE.toString() + "/" + + id.hashCode() + file.getOriginalFilename();

        minioService.saveFile(file);

        return newUrl;
    }

    private String getFileName(String url) {
        String[] args = url.split("/");
        return args[3];
    }
}
