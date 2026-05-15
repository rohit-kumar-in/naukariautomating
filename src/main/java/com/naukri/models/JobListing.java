package com.naukri.models;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListing {
    private String title;
    private String company;
    private String location;
    private String experience;
    private String url;
    private boolean easyApply;
    private boolean applied;
    private String platform;
    private String status; // "APPLIED", "SKIPPED", "FAILED"
    private String reason;
}
