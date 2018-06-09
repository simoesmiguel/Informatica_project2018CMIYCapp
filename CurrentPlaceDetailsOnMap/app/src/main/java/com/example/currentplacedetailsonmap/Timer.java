package com.example.currentplacedetailsonmap;

public interface Timer {
    interface TimerCallback {
        boolean tick();
    }


    void start();
    void stop();
}