package com.example.pws.service;

import com.example.pws.domain.Scenario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.util.*;


@Service
public class ScenarioService {
    private final Map<String, Scenario> scenarios = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();


    @PostConstruct
    void load() throws Exception {
        var res = new ClassPathResource("scenarios/basic-linux.json");
        Scenario s = mapper.readValue(res.getInputStream(), Scenario.class);
        scenarios.put(s.getId(), s);
    }


    public Optional<Scenario> get(String id) { return Optional.ofNullable(scenarios.get(id)); }
    public Collection<Scenario> list() { return scenarios.values(); }
}