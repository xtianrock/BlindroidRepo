package com.Xtian.Blindroid;

import android.content.ContentValues;
import android.content.Context;

/**
 * Clase para el objeto contacto
 */
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

    /**
     * obtiene el nombre del contacto
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del contacto
     * @param nombre
     */
    public void setName(String nombre) {
        this.name = nombre;
    }

    /**
     * obtiene el nombre cmopleto del contacto
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * obtiene el telephono del contacto
     * @return
     */
    public String getPhone() {
        return phone;
    }
    /**
     * obtiene el id del contacto
     * @return
     */
    public long getID() {
        return id;
    }

    /**
     * Registra al contacto en el servidor.
     * @param context
     */
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

    /**
     * incrementa en uno el contador de mensajes no leidos
     * @param context
     */
    public void unreadMessage(Context context)
    {
        DataProvider.incrementCount(context,this.phone.replace("+",""));
    }

    @Override
    public String toString() {
        return this.fullName;
    }



}
