package com.example.pws.dto;

import java.time.Instant;


public record SessionView(String id, String status, Instant startedAt, Long ttlSeconds) {}