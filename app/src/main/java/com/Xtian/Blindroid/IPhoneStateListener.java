package com.Xtian.Blindroid;

/**
 * Created by xtianrock on 17/04/2015.
 */
public interface IPhoneStateListener {

    public void onStateIdle();
    public void onStateRinging();
    public void onStateOffhook();
    public void onStateCall();
}
