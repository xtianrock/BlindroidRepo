package com.example.blindroid;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
 
public class OnOffReceiver extends BroadcastReceiver {
 
        private boolean screenOff;
 
        @Override
        public void onReceive(Context context, Intent intent) {           
            
        	 SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        	  boolean bloqueo=prefs.getBoolean("bloqueo", false);
             
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                 
                screenOff = true;
                screenOnOff(context);
                 
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                 
            
              if (!bloqueo)
              {
            	  KeyguardManager km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
                  boolean locked = km.inKeyguardRestrictedInputMode();
                  if (!locked)
                  {
                  	screenOff = false;
                  	screenOnOff(context);
                  }
              }
              else
              {
            	  screenOff = false;
                	screenOnOff(context);
              }
              
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) 
            {
            	  if (!bloqueo) 
            	  {
            		  screenOff = false;
                  	screenOnOff(context);
            	  }
            	
            }   
                 
            
          
           
        	// LANZAR SERVICIO
        	
             
            // Toast.makeText(context, "BroadcastReceiver :"+screenOff, Toast.LENGTH_SHORT).show();
             
          
        }

		private void screenOnOff(Context context) {
			// Crea el servicio y le informa el estado de la pantalla
            Intent i = new Intent(context, ServiceBoot.class);
            i.putExtra("screen_state", screenOff);
            context.startService(i);
		}
 
    }
