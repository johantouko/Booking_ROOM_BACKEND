package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.ServiceImpl.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@CrossOrigin(origins = "*")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {

        this.sseService = sseService;
    }

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToSse(@PathVariable Long userId) {
        return sseService.subscribe(userId);
    }
}
