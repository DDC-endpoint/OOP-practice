package edu.practice.d11;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Run me (green triangle) to start the API on http://localhost:8080 and try it with curl. */
@SpringBootApplication
public class MeetingRoomsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomsApplication.class, args);
    }
}
