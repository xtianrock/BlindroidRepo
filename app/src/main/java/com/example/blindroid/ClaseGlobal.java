package com.example.blindroid;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;

import java.util.List;

public class ClaseGlobal extends Application{
	
	boolean appLlamando=false;
	boolean screenOff=false;
	boolean busquedaIntalada=false;
	
	public void setLlamando(boolean llamando)
	{
		appLlamando=llamando;
	}
	
	public boolean getLlamando()
	{
		return appLlamando;
	}	
	
	
	public void setScreenOff(boolean screenOff)
	{
		this.screenOff=screenOff;
	}
	
	public boolean getScreenOff()
	{
		return screenOff;
	}
	
	
	public void setBusquedaInstalada(boolean instalada)
	{
		busquedaIntalada=instalada;
	}
	
	public boolean getBusquedaInstalada()
	{
		return busquedaIntalada;
	}	
	
	
	 public String reemplazarCaracteresRaros(String input) {
	    // Cadena de caracteres original a sustituir.
	    String original = "áàäéèëíìïóòöúùuÁÀÄÉÈËÍÌÏÓÒÖÚÙÜ";
	    // Cadena de caracteres ASCII que reemplazar�n los originales.
	    String ascii = "aaaeeeiiiooouuuAAAEEEIIIOOOUUU";
	    String output = input;
	    for (int i=0; i<original.length(); i++) {
	        // Reemplazamos los caracteres especiales.
	        output = output.replace(original.charAt(i), ascii.charAt(i));
	    }//for i
	    return output;
	}
	 
	 public boolean existePaquete(String paquete) {
		    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		    List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
		    final List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
		    boolean packageExists = false;
		    boolean activityExists = false;
		    for(PackageInfo r:packs){
		        String pkg = r.applicationInfo.packageName;
		        if(pkg != null && pkg.equals(paquete)){
		            packageExists = true;
		            break;
		        }
		    }
		    if(packageExists == false)
		        return false;

		    for(ResolveInfo r:pkgAppsList){ 
		        ActivityInfo info = r.activityInfo;
		        if(info!=null && info.name!=null && info.name.equals("com.google.android.googlequicksearchbox.VoiceSearchActivity")){
		            activityExists = true;
		            break;
		        }
		    }
		    return activityExists;
		}



	 

}
