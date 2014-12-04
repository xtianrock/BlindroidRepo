package com.Xtian.Blindroid;

public interface AccelerometerListener {

    public void onAccelerationChanged(float x, float y, float z);

    public void onShake(float force);

    public void onDubleShake(float force);

}