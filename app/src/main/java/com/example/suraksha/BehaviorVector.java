package com.example.suraksha;

public class BehaviorVector {
    public double typingSpeed;
    public double dwellTime;
    public double flightTime;
    public int backspaceCount;
    public double tapPressure;
    public int swipeDirection;
    public double tiltAngle;

    public BehaviorVector(double typingSpeed, double dwellTime, double flightTime, int backspaceCount,
                          double tapPressure, int swipeDirection, double tiltAngle) {
        this.typingSpeed = typingSpeed;
        this.dwellTime = dwellTime;
        this.flightTime = flightTime;
        this.backspaceCount = backspaceCount;
        this.tapPressure = tapPressure;
        this.swipeDirection = swipeDirection;
        this.tiltAngle = tiltAngle;
    }

    @Override
    public String toString() {
        return "Vector[" +
                "TypingSpeed=" + typingSpeed +
                ", DwellTime=" + dwellTime +
                ", FlightTime=" + flightTime +
                ", BackspaceCount=" + backspaceCount +
                ", TapPressure=" + tapPressure +
                ", SwipeDirection=" + swipeDirection +
                ", TiltAngle=" + tiltAngle +
                ']';
    }
}
