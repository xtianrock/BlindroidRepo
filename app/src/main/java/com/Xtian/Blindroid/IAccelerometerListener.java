package com.Xtian.Blindroid;

public interface IAccelerometerListener {

    public void onAccelerationChanged(float x, float y, float z);

    public void onShake(float force);

    public void onDoubleShake(float force);

}