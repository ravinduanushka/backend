package com.project.mentorservice.controller;

import com.project.mentorservice.dto.InternalMentorByUserResponse;
import com.project.mentorservice.dto.InternalMentorResponse;
import com.project.mentorservice.dto.InternalSlotResponse;
import com.project.mentorservice.dto.RatingRequest;
import com.project.mentorservice.dto.RatingResponse;
import com.project.mentorservice.dto.SlotStatusRequest;
import com.project.mentorservice.service.InternalMentorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalMentorController {

    private final InternalMentorService internalMentorService;

    public InternalMentorController(InternalMentorService internalMentorService) {
        this.internalMentorService = internalMentorService;
    }

    @GetMapping("/mentors/{id}")
    public InternalMentorResponse getMentor(@PathVariable String id) {
        return internalMentorService.getMentor(id);
    }

    @GetMapping("/mentors/by-user/{userId}")
    public InternalMentorByUserResponse getMentorByUser(@PathVariable String userId) {
        return internalMentorService.getMentorByUser(userId);
    }

    @PutMapping("/slots/{slotId}/status")
    public InternalSlotResponse changeSlotStatus(@PathVariable String slotId,
                                                 @Valid @RequestBody SlotStatusRequest request) {
        return internalMentorService.changeSlotStatus(slotId, request.status());
    }

    @PostMapping("/mentors/{id}/rating")
    public RatingResponse addRating(@PathVariable String id, @Valid @RequestBody RatingRequest request) {
        return internalMentorService.addRating(id, request.rating());
    }
}