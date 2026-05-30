package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ApodService {
    Mono<ApodResponse> getApod(LocalDate date);
}
