package com.m2049r.xmrwallet.model;

/**
 */

public class CandidateInfo {

    private String name;
    private String lastname;
    private String address;
    private String voting;

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getAddress() {
        return address;
    }

    public String getVoting() {
        return voting;
    }

    public CandidateInfo(String name, String lastname, String address) {
        this.name = name;
        this.lastname = lastname;
        this.address = address;this.voting = voting;
    }

    public CandidateInfo(String name, String lastname, String address, String voting) {
        this.name = name;
        this.lastname = lastname;
        this.address = address;
        this.voting = voting;
    }
}
