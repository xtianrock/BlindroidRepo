package com.example.blindroid;

import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class TextToSpeakService extends Service implements TextToSpeech.OnInitListener {

   TextToSpeech tts;
   String numero;
   String nombre;
   int volRing;

   @Override
   public void onCreate() {
      super.onCreate();        
      
              

      
     
   }
   
   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {   	   
	numero=intent.getStringExtra("numero");	
	nombre=obtenerNombre(numero);	
	Toast.makeText(getBaseContext(), numero,Toast.LENGTH_SHORT).show();
	tts = new TextToSpeech(this, this); 
	return START_NOT_STICKY;   
	   
   }
   
   @Override
   public void onInit(int status) {
       if (status == TextToSpeech.SUCCESS) {
    	   Handler handler = new Handler();
    	 
    		
    	   tts.setLanguage(Locale.getDefault());
    	   tts.speak(nombre, TextToSpeech.QUEUE_FLUSH, null); 
    	   Runnable runnable = new Runnable() {
     		   @Override
     		   public void run() {
     			  AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);    	  
     	    	   volRing = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);  	        	    	         
     	    	   mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);  
     		   }
     		};
    	   handler.postDelayed(runnable, 1500);
    	  
    	   Runnable runnable2 = new Runnable() {
    		   @Override
    		   public void run() {
    			   AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);  
    				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, volRing, 0);
    	        	stopSelf();    		    
    		   }
    		};
    	   handler.postDelayed(runnable2, 3000);
        
        	  
        	
       } else {
           Log.e("TTS", "Initilization Failed!");
       }
       

   }
   
   
   @Override
   public void onDestroy(){
      if(tts !=null){
         tts.stop();
         tts.shutdown();
      }
      super.onDestroy();
   }
  
   public void speakOut(String nombre){
     
      tts.speak(nombre, TextToSpeech.QUEUE_FLUSH, null);

   }
   
   
   public String obtenerNombre(String numero)
   {
	   for(int a=0;a<ServiceBoot.contactos.size();a++)
		{	   
		   String telefono=ServiceBoot.contactos.get(a).getTelefono().replaceAll(" ","");		   
   			if(telefono.replace("+34","").equals(numero))
   			{	    	   
   				return ServiceBoot.contactos.get(a).getNombre();  		
   		
   			}
   		
		}
	   return null;
   }

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}


   
}