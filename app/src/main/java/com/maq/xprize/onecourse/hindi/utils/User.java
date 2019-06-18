package com.maq.xprize.onecourse.hindi.utils;

public class User {

    private String UserName;
    private int Age;
    private String ModuleName;
    private long StartTime;
    private long EndTime;
    private long ElapseTIme;


    public User (){}


    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public String getModuleName() {
        return ModuleName;
    }

    public void setModuleName(String moduleName) {
        ModuleName = moduleName;
    }

    public long getStartTime() {
        return StartTime;
    }

    public void setStartTime(long startTime) {
        StartTime = startTime;
    }

    public long getEndTime() {
        return EndTime;
    }

    public void setEndTime(long endTime) {
        EndTime = endTime;
    }

    public long getElapseTIme() {
        return ElapseTIme;
    }

    public void setElapseTIme(long elapseTIme) {
        ElapseTIme = elapseTIme;
    }

    public User(String userName, int age, String moduleName, long startTime, long endTime, long elapseTIme) {
        UserName = userName;
        Age = age;
        ModuleName = moduleName;
        StartTime = startTime;
        EndTime = endTime;
        ElapseTIme = elapseTIme;
    }


}
