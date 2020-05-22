package com.qittiq.avolkov.challenge.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Statistics {
    private long sum;
    private long avg;
    private long max;
    private long min;
    private int count;
}

