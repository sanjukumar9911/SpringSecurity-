package com.ford.vdcc.poc.controller;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestObject implements Serializable {
    private String applicationCode;
    private String applicationType;
    private EventDetails eventDetails;
}
