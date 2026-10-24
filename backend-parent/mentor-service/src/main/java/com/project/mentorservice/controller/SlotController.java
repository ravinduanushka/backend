package com.project.mentorservice.controller;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.SlotRequest;
import com.project.mentorservice.dto.SlotResponse;
import com.project.mentorservice.entity.SlotStatus;
import com.project.mentorservice.service.SlotService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/me/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(CurrentUser current, @Valid @RequestBody SlotRequest request) {
        return slotService.create(current, request);
    }

    @GetMapping("/me/slots")
    public List<SlotResponse> listMine(CurrentUser current,
                                       @RequestParam(required = false) SlotStatus status,
                                       @RequestParam(required = false) String from,
                                       @RequestParam(required = false) String to) {
        return slotService.listMine(current, status, from, to);
    }

    @PutMapping("/me/slots/{slotId}")
    public SlotResponse update(CurrentUser current, @PathVariable String slotId,
                               @Valid @RequestBody SlotRequest request) {
        return slotService.update(current, slotId, request);
    }

    @DeleteMapping("/me/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(CurrentUser current, @PathVariable String slotId) {
        slotService.delete(current, slotId);
    }

    @GetMapping("/{id}/slots")
    public List<SlotResponse> listForMentor(CurrentUser current, @PathVariable String id,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        return slotService.listForMentor(current, id, from, to);
    }
}