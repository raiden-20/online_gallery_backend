package ru.vsu.cs.sheina.online_gallery_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class OnlineGalleryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineGalleryBackendApplication.class, args);
    }
}
