package com.example.blindroid;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class ServiceBoot extends Service implements AccelerometerListener {

   
    static ArrayList<Contacto>contactos=new ArrayList<Contacto>();
    boolean screenOff;
    SharedPreferences prefs;
    BroadcastReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
       
        
       // instancio el receiver que controla el estado de la pantalla.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new OnOffReceiver();
        registerReceiver(mReceiver, filter);
        Log.i("xtian", "Servicio creado yreceiver registrado");
        
        
         prefs= PreferenceManager.getDefaultSharedPreferences(this);
        

        //instancio el listener que detecta si se esta en una llamada o no
        TelephonyManager telephonyManager =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);     
        PhoneStateListener callStateListener = new PhoneStateListener() {  
        	public void onCallStateChanged(int state, String incomingNumber)   
        	{ 	      
        		if(state==TelephonyManager.CALL_STATE_IDLE)
        		{  
        			activarAcel();
        			//Cambia a false la variable global que permite saber si la llamada ha sido realizada con la aplicacion
        			final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
    	    		vGlobal.setLlamando(false);
    	    		
        		}
        		else if(state==TelephonyManager.CALL_STATE_RINGING)
        		{
        			comprobarYpararAcel();
        			tts(incomingNumber);
        			//Toast.makeText(getBaseContext(), "Llamada entrante: "+incomingNumber,Toast.LENGTH_SHORT).show();
        		}
        		else if(state==TelephonyManager.CALL_STATE_OFFHOOK)
        		{
        			llamando();  	        			
        	
        		}
        	} 

			

			
        };  
        telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);  
   
      
        Toast.makeText(getBaseContext(), "servicio creado", 
                Toast.LENGTH_SHORT).show();
         
        getNameNumber();

		
       
}
    
    
    private void llamando() 
	{				
		AccelerometerManager.stopListening();
		final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();		
		boolean speaker=prefs.getBoolean("speaker", false);
		 
		 if(vGlobal.appLlamando)
		{
			if(speaker)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				
				if (!mAudioManager.isSpeakerphoneOn())
				{
					
					mAudioManager.setSpeakerphoneOn(true);
				}
			}
		}
		
		
		
	}  


    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
       
      //  Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
       // v.vibrate(200);
       // Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        
        try{          
            screenOff = intent.getBooleanExtra("screen_state", false);
             
        }catch(Exception e){}
      if (!screenOff) 
      {
    	 	 activarAcel();
    	      //	 Toast.makeText(getBaseContext(), "acel activado",Toast.LENGTH_SHORT).show();  	        
      } 
      else 
      {
    	 
    		 comprobarYpararAcel();     
    	       //  Toast.makeText(getBaseContext(), "acel desactivado",Toast.LENGTH_SHORT).show();
      }
      Log.i("xtian", "onStartCommand del servicio, screenOff:"+screenOff);
      
   //  tts("626047737");
        // con sticky mantengo el servicio hasta que se le ordene parar
        return START_STICKY;
    }

	private void activarAcel() {
		
	comprobarYpararAcel();
		String sensibilidad=prefs.getString("sensibilidad", "18");
		if (AccelerometerManager.isSupported(this)) {
               
               //Start Accelerometer Listening
               AccelerometerManager.startListening( this,Integer.parseInt(sensibilidad));
               
           }
	}
	
	
	private void tts(String numero)
	{
		String sp;
		boolean speak=prefs.getBoolean("speak", false);
		if (speak){
			 sp="true";
		}
		else
		{
			sp="false";
		}
		 
		if (speak)
		{
			Toast.makeText(getBaseContext(), sp,Toast.LENGTH_SHORT).show();
			Intent i =new Intent(ServiceBoot.this,TextToSpeakService.class);
			i.putExtra("numero",numero);
			startService(i);
		}		    
	}


       @Override
    public void onDestroy() {
        super.onDestroy();

        //Si el sensor esta activo lo desactivo
        if (AccelerometerManager.isListening()) {              
            AccelerometerManager.stopListening();             
            Toast.makeText(getBaseContext(), "serv. y acel. destruidos",Toast.LENGTH_SHORT).show();
        }         
        unregisterReceiver(mReceiver);
        contactos.clear();
        Log.i("xtian", "service onDestroy");
    }
    
    
    private void getNameNumber(){ 
    	
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
       ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
              ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor names = getContentResolver().query(
       uri, projection, null, null, null);
        int indexName = names.getColumnIndex(
       ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = names.getColumnIndex(
       ContactsContract.CommonDataKinds.Phone.NUMBER);
        names.moveToFirst();
        do {
           //Aqu� relleno los dos
        ClaseGlobal vGlobal=new ClaseGlobal();        
        //Reemplazo los caracteres acentuados por los normales            
            String nombre=vGlobal.reemplazarCaracteresRaros(names.getString(indexName)).toLowerCase();
        	String numero=names.getString(indexNumber).replace("+34","");
           Contacto cont=new Contacto(nombre,numero.replace(" ",""));
           contactos.add(cont);
        } while (names.moveToNext());
        names.close();

       
    }
    
    
    private void comprobarYpararAcel() {
		if(AccelerometerManager.isListening())
		{
			AccelerometerManager.stopListening();
		}
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShake(float force) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);	      
	    v.vibrate(200);
	  
		Intent i =new Intent(this,ReconocimientoVoz.class);
	    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);	    
	    getApplication().startActivity(i);
	    Log.i("xtian", "OnShake");
		
		
	}
}

