package com.example.blindroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

 


public class ReconocimientoVoz extends Activity {
	
	 private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
     String accion;





    @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	 getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		      | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	  setContentView(R.layout.reconocimiento);	 
	
	 // Log.i("xtian", "oncreate");
	


	  Handler handler = new Handler();  		
 	   Runnable runnable = new Runnable() {
  		   @Override
  		   public void run() {
  			startVoiceRecognitionActivity();
  		   }
  		};
 	   handler.postDelayed(runnable, 150);
	  
	 
	 }
	
	 
	 @Override
	 public void onStart() {
	     super.onStart();  // Always call the superclass method first
	    // Log.i("xtian", "onstart"); 
	    
	 }
	 
	 
	 protected void onResume(){
	        super.onResume();
	        
	      //  Log.i("xtian", "onResume");
	    }
	
	 
	 public void startVoiceRecognitionActivity() {
		// Log.i("xtian", "reconocimiento activado");

	  // Definici�n del intent para realizar en an�lisis del mensaje
	  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	  // Indicamos el modelo de lenguaje para el intent
	  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	  // Definimos el mensaje que aparecer�
      SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(ReconocimientoVoz.this);
	  intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Diga "+prefs.getString("hotword_call","")+" o "+prefs.getString("hotword_chat","")+" y el nombre del contacto ...");
	  // Lanzamos la actividad esperando resultados
	  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	 }
	 
	 @Override
	 //Recogemos los resultados del reconocimiento de voz
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  //Si el reconocimiento a sido bueno
	  if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
	  //El intent nos envia un ArrayList aunque en este caso solo 
	   //utilizaremos la pos.0
	    ArrayList<String> resultados = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
          SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(ReconocimientoVoz.this);


          modoDepuracion(resultados,prefs);
          String hotword=vGlobal.reemplazarCaracteresRaros(prefs.getString("hotword","").toLowerCase());


         accion = obtenerAccion(resultados, vGlobal, hotword,prefs);
         ejecutarAccion(resultados, vGlobal,prefs);


          finish();
	    	
	    	
	 
	    }
	   else
	   { 
		 
		  finish();
	   }
	  }



    private void ejecutarAccion(ArrayList<String> resultados, ClaseGlobal vGlobal,SharedPreferences prefs) {
        int length=resultados.size();
        bucle:
        for (int i=0; i<length;i++)
        {
            String nombre = vGlobal.reemplazarCaracteresRaros(resultados.get(i)).toLowerCase();
            nombre=nombre.replace(prefs.getString("hotword","")+" ","");
            for(int a=0;a< ServiceBoot.contactos.size();a++)
            {
                if(ServiceBoot.contactos.get(a).getNombre().equals(nombre))
                {

                    if(accion.equals("chat"))
                    {
                        String numero=ServiceBoot.contactos.get(a).getTelefono();
                        openWhatsApp("34"+numero+"@s.whatsapp.net");
                    }
                    else if(accion.equals("llamar"))
                    {
                        vGlobal.setLlamando(true);
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + ServiceBoot.contactos.get(a).getTelefono()));
                        startActivity(callIntent);
                   }
                    finish();
                    break bucle;
                }
            }
        }
    }

    private String obtenerAccion(ArrayList<String> resultados, ClaseGlobal vGlobal, String hotword,SharedPreferences prefs){
        int length=resultados.size();
        for (int i=0; i<length;i++)
        {
            accion=prefs.getString("accion","llamar");
            String nombre = vGlobal.reemplazarCaracteresRaros(resultados.get(i)).toLowerCase();
            String[] nombre2 = nombre.split(" ");
            if (nombre2[0].equals(hotword))
            {
                if(prefs.getString("accion","llamar").equals("llamar")) {
                    accion = "chat";
                    return accion;
                }
                    else
                {
                    accion = "llamar";
                    return accion;
                }
            }


       }
       return accion;
    }

    private void modoDepuracion(ArrayList<String> resultados,SharedPreferences prefs) {
        boolean depuracion=prefs.getBoolean("depuracion", false);
        if(depuracion)
        {
            String texto ="";
             for (int i=0;i<resultados.size();i++)
             {
                 texto+=" "+resultados.get(i);
             }
              Toast.makeText(getBaseContext(), texto, Toast.LENGTH_SHORT).show();
        }
    }


    private void openWhatsApp(String id) {

        final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
        boolean whatsappInstalado= vGlobal.existePaquete("com.whatsapp");
        if (whatsappInstalado)
        {
            Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[] { ContactsContract.Contacts.Data._ID }, ContactsContract.Data.DATA1 + "=?",
                    new String[] { id }, null);
            c.moveToFirst();
            Intent whatsapp = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + c.getString(0)));
            c.close();
            startActivity(whatsapp);

        }


         else {


           Toast.makeText(this, "Whatsapp necesario", Toast.LENGTH_SHORT)
                    .show();

            Uri uri = Uri.parse("market://details?id=com.whatsapp");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goToMarket);
        }


    }



    //Con el getNameNumber lo que hago es recoger los nombres
	 //de la SIM en un vector
	 //Y los numeros de telefonos en otro vector, eso s� tienen que coincidir
	 //las posiciones de uno y de otro, por eso los relleno a la vez.
	 protected void onPause(){
	        super.onPause();
	        
	     
	    }
	
	
	
	@Override
    public void onStop() {
        super.onStop();
       
    }
}