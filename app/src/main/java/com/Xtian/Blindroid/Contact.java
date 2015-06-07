package com.Xtian.Blindroid;

import android.content.ContentValues;
import android.content.Context;

public class Contact {
    private long id;
    private String name;
    private String fullName;
    private String phone;

    public Contact(long id, String name, String fullName, String phone) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String nombre) {
        this.name = nombre;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public long getID() {
        return id;
    }

    public void register(Context context)
    {
        if (!DataProvider.exist(context, this.phone.replace("+","")))
        {
            ContentValues profileValues = new ContentValues(2);
            profileValues.put(DataProvider.COL_NAME, fullName);
            profileValues.put(DataProvider.COL_PHONE, phone.replace("+",""));
            context.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, profileValues);
        }

    }
    public void unreadMessage(Context context)
    {
        DataProvider.incrementCount(context,this.phone.replace("+",""));
    }

    @Override
    public String toString() {
        return this.fullName;
    }



}
