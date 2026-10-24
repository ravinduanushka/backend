package com.project.bookingservice.service;

import com.project.bookingservice.client.MentorClient;
import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class BookingAccess {

    private final MentorClient mentorClient;

    public BookingAccess(MentorClient mentorClient) {
        this.mentorClient = mentorClient;
    }

    public boolean isStudentOf(Booking booking, CurrentUser current) {
        return booking.getStudentId().equals(current.id());
    }

    public boolean isMentorOf(Booking booking, CurrentUser current) {
        if (current.role() != Role.MENTOR) {
            return false;
        }
        return mentorClient.getMentor(booking.getMentorId())
                .map(m -> m.userId().equals(current.id()))
                .orElse(false);
    }
}