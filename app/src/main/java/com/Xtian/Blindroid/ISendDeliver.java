package com.Xtian.Blindroid;

/**
 * Created by xtianrock on 12/05/2015.
 */
public interface ISendDeliver {

    public void delivered();

    public void notDelivered();

    public void sent();

    public void genericFailure();

    public void noService();

    public void nullPdu();

    public void radioOff();
}
