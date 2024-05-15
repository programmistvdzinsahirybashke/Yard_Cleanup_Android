package com.example.myapplication;

public class UserData {
    private static int userID;

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        UserData.userID = userID;
    }
}
