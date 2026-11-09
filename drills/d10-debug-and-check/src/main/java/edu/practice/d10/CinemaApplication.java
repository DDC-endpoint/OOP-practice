package edu.practice.d10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Run me (green triangle) to start the API on http://localhost:8080 and check it with curl. */
@SpringBootApplication
public class CinemaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinemaApplication.class, args);
    }
}
