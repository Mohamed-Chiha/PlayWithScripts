package com.example.pws.domain;


import lombok.*;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scenario {
    private String id;
    private String title;
    private String description;
    private List<Step> steps;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Step {
        private String command;
        private String hint;
    }
}