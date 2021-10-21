package com.ford.vdcc.poc.controller;

import lombok.Data;

import java.io.Serializable;

@Data
public class EventDetails implements Serializable {

    private String eventType;
    private String errorCode;
    private String errorDetails;
    private String eventTime;

}
