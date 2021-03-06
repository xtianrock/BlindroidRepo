package com.Xtian.Blindroid;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.Xtian.Blindroid.Gcm.ServerUtilities;

import java.io.IOException;

/**
 * Created by xtianrock on 20/05/2015.
 */

/**
 * Clase para el objeto contacto
 */
public class Message {
    private String text;
    private String sender;
    private String receiver;
    private int type;

    /**
     * constructor por defecto
     */
    public Message (){}


    /**
     * Constructor
      * @param text
     * @param sender
     * @param receiver
     * @param type
     */
    public Message (String text, String sender,String receiver,int type)
    {
        this.text=text;
        this.sender=sender;
        this.receiver=receiver;
        this.type=type;
    }

    /**
     * Obtiene el telefono del emisor
     * @return
     */
    public String getSender() {
        return sender;
    }

    /**
     * Establece el telefono del emisor
     * @param sender
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Establece el texto del mensaje
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * obtiene el texto del mensaje
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Establece el receptor del mensaje
     * @param receiver
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Obtiene el receptor del mensaje
     * @return
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Establece el tipo de mensaje
     * @param type
     */
    public void setType(int type) {this.type = type;}

    /**
     * Almacena el mensaje en la base de datos
     * @param context
     */
    public void register(Context context){

        ContentValues values = new ContentValues(2);
        values.put(DataProvider.COL_TYPE, this.type);
        values.put(DataProvider.COL_MESSAGE, this.text);
        values.put(DataProvider.COL_RECEIVER_PHONE, this.receiver);
        values.put(DataProvider.COL_SENDER_PHONE, this.sender);
        context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
    }

    /**
     * Envia el mensaje al servidor
     * @param context
     */
    public void send(final Context context) {
        final String text=this.text;
        final String upperText = text.substring(0,1).toUpperCase() + text.substring(1);
        final String receiver=this.receiver.replace("+", "").trim();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    msg = ServerUtilities.send(upperText, receiver);
                }  catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                Log.i("estado mensaje",msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }


}

