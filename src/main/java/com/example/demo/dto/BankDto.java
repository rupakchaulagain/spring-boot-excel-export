package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class BankDto implements Serializable {

    private String name;
    private String code;
    private String address;
    private String phone;
    private double amount;
    private String type;
    private String level;
    private String branch;
    private Date date;
    private int numberOfDepartment;
}
