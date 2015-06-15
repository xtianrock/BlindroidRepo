package com.Xtian.Blindroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.RemoteInput;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Clase encargada de procesar el reconocimiento de voz
 */
public class RecognitionActivity extends Activity {

    public static final String PREDEFINED_REPLY = "Ok";

    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    public static final String WEAR_REPLY = "wear_reply";
    public static final String PHONE_REPLY = "phone_reply";
    public static final String QUICK_REPLY = "quick_reply";
    public static final String REPLY_NUMBER = "reply_number";
    Dialog customDialog;
    private static final int CALL = 0;
    private static final int MESSAGE = 1;
    private static final int BUILDING_MESSAGE = 2;
    private static final String keyword = "mensaje";
    private static final String TAG = "reco";
    private Contact messageContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.reconocimiento);
        messageContact = null;

        Intent intent=getIntent();
         if (intent.hasExtra(REPLY_NUMBER))
        {
            if(intent.getAction()==PHONE_REPLY)
            {
                String phone = intent.getExtras().getString(REPLY_NUMBER);
                messageContact=Commons.getContactByPhone(phone);
            }
            if(intent.getAction()==WEAR_REPLY)
            {
                String phone = intent.getExtras().getString(REPLY_NUMBER);
                String msg= getMessageText(intent).toString();
                Message message =new Message(msg,Commons.getPhoneNumber(),phone,DataProvider.MessageType.OUTGOING.ordinal());
                send(message);
            }
            if(intent.getAction()==QUICK_REPLY)
            {
                String phone = intent.getExtras().getString(REPLY_NUMBER);

                Message message =new Message(PREDEFINED_REPLY,Commons.getPhoneNumber(),phone,DataProvider.MessageType.OUTGOING.ordinal());
                send(message);
            }
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                SpeechRecognitionHelper.run(RecognitionActivity.this,R.string.prompt);

            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 150);
    }

    /**
     * Obtiene el texto de un mensaje enviado desde Android Wear
     * @param intent
     * @return
     */
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }
    @Override
    public void onStart() {
        super.onStart();
        Tracker t = ((Commons) getApplication()).getTracker(Commons.TrackerName.APP_TRACKER);
        t.setScreenName("Reconocimiento");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }



    @Override
    //Recogemos los resultados del reconocimiento de voz
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SpeechRecognitionHelper.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            int mode = CALL;
            for (String result : results) {
                if (messageContact!=null)
                {
                    mode = BUILDING_MESSAGE;
                    break;
                }
                else if (result.contains(keyword)) {
                    mode = MESSAGE;
                    break;
                }
            }
            switch (mode) {
                case CALL:
                    actionCall(results);
                    break;
                case MESSAGE:
                    actionMessage(results);
                    break;
                case BUILDING_MESSAGE:
                    actionBuildMessage(results);
                    break;
            }
        }
        else{
            finish();
        }
    }

    /**
     * Procesa el resultado del reconocimiento de voz como llamada
     * @param results
     */
    private void actionCall(ArrayList<String> results)
    {
        ArrayList<Contact>contacts = Commons.getMatchingContacts(results);
        switch (contacts.size())
        {
            case 0:
                dialog(null,CALL);
                break;
            case 1:
                callContact(contacts.get(0));
                break;
            default:
                dialog(contacts,CALL);
                break;
        }
    }

    /**
     * Procesa el resultado del reconocimiento de voz como mensaje
     * @param results
     */
    private void actionMessage(ArrayList<String> results)
    {
        ArrayList<String> names = processResults(results);

        ArrayList<Contact>contacts = Commons.getMatchingContacts(names);
        switch (contacts.size())
        {
            case 0:
               dialog(null,MESSAGE);
                break;
            case 1:
                messageContact=contacts.get(0);
                SpeechRecognitionHelper.run(this,R.string.prompt);
                break;
            default:
              dialog(contacts,MESSAGE);
                break;
        }
    }

    /**
     * Elimina la palabra clave de cada elemento del ArryaList
     * @param results
     * @return
     */
    private ArrayList<String> processResults(ArrayList<String> results)
    {
        ArrayList<String> posibleContacts=new ArrayList<>();
        for (String result:results)
        {
            String name=result.replaceAll(keyword,"").trim();
            Log.i(TAG, name);
            posibleContacts.add(name);
        }
        return posibleContacts;
    }

    /**
     * Construye el mensaje y s elo pasa al metodo confirmationDialog
     * @param results
     */
    private void actionBuildMessage(ArrayList<String> results)
    {
        String msg=results.get(0);
        String phone=messageContact.getPhone().replace("+","").trim();
        msg = Character.toUpperCase(msg.charAt(0)) + msg.substring(1);
        Message message=new Message(msg,Commons.getPhoneNumber(),phone,DataProvider.MessageType.OUTGOING.ordinal());
        confirmationDialog(message);

    }

    /**
     * Envia el mensaje
     * @param message mensaje a enviar
     */
    private void send(Message message) {
        if(messageContact!=null)
        messageContact.register(this);
        message.send(this);
        message.register(this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
        finish();
    }

    /**
     * Llama al contacto
     * @param contact
     */
    private void callContact(Contact contact) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" +contact.getPhone()));
        startActivity(callIntent);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        //finish();
    }

    /**
     * Genera un cuadro de dialog con los resultados
     * @param contacts
     * @param mode
     */
    private void dialog(final List<Contact> contacts,final int mode) {

        customDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setCancelable(false);

        if(contacts!=null)
        {
            customDialog.setContentView(R.layout.dialog);

            ListView dialog_ListView = (ListView)customDialog.findViewById(R.id.dialoglist);
            ArrayAdapter<Contact> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, contacts);
            dialog_ListView.setAdapter(adapter);
            setListViewHeightBasedOnChildren(dialog_ListView);
            dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    customDialog.dismiss();
                    switch(mode)
                    {
                        case MESSAGE:
                            messageContact=contacts.get(position);
                            SpeechRecognitionHelper.run(RecognitionActivity.this,R.string.prompt);
                            break;
                        case CALL:
                            callContact(contacts.get(position));
                    }
                }});
        }
        else
        {
            customDialog.setContentView(R.layout.dialog_no_contact);

            TextView contenido = (TextView) customDialog.findViewById(R.id.contenido);
            contenido.setText(R.string.contact_not_found);


            if (!isOnline()) {
                TextView internet = (TextView) customDialog.findViewById(R.id.internet);
                internet.setVisibility(View.VISIBLE);

                View separador = customDialog.findViewById(R.id.divider2);
                separador.setVisibility(View.VISIBLE);
            }
        }

        customDialog.findViewById(R.id.aceptar).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                SpeechRecognitionHelper.run(RecognitionActivity.this,R.string.prompt);

            }
        });

        customDialog.findViewById(R.id.cancelar).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                finish();

            }
        });
        customDialog.show();
    }


    /**
     * Genera un cuadro de dialogo que permite
     * confirmar o cancelar el envio de un mensaje
     * @param message
     */
    private void confirmationDialog(final Message message) {

        customDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setCancelable(false);
        customDialog.setContentView(R.layout.confirmation_dialog);

        final Button btCancel= (Button)customDialog.findViewById(R.id.cancel);
        final Button btRetry= (Button)customDialog.findViewById(R.id.retry);

        TextView name = (TextView) customDialog.findViewById(R.id.contact_name);
        name.setText(messageContact.getFullName());

        TextView contenido = (TextView) customDialog.findViewById(R.id.message);
        contenido.setText(message.getText());
        ImageView contactPhoto= (ImageView)customDialog.findViewById(R.id.contact_photo);

        Bitmap bitmap= BitmapFactory.decodeStream(Commons.openPhoto(this, message.getReceiver()));
        RoundedBitmapDrawable dr =RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        dr.setCornerRadius(Math.min(dr.getMinimumWidth(),dr.getMinimumHeight()));
        contactPhoto.setImageDrawable(dr);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String duration=sharedPreferences.getString("timeout","5");
        Log.i("duration",String.valueOf(1/Float.parseFloat(duration)));
        float time=(1/Float.parseFloat(duration));

        final ProgressWheel wheel= (ProgressWheel)customDialog.findViewById(R.id.progress_wheel);
        wheel.setProgress(100);
        wheel.setSpinSpeed(time);

        wheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRetry.setVisibility(View.VISIBLE);
                btCancel.setVisibility(View.VISIBLE);
                wheel.setVisibility(View.GONE);
            }
        });

        wheel.setCallback(new ProgressWheel.ProgressCallback() {
            @Override
            public void onProgressUpdate(float progress) {
                if(progress == 1.0f) {
                   customDialog.dismiss();
                   send(message);
                }
            }
        });
        btRetry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                SpeechRecognitionHelper.run(RecognitionActivity.this,R.string.prompt);

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                finish();

            }
        });

        customDialog.show();
    }


    /**
     * Comprueba si existe acceso a internet
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * Adapta el tamano del listView al numero de elementos
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = ListView.MeasureSpec.makeMeasureSpec(listView.getWidth(), ListView.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            listView.measure(desiredWidth, ListView.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }



}