package org.apache.cordova.plugin.Brightness;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.view.Window;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.widget.RemoteViews;
import android.widget.Toast;

import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import static java.security.AccessController.getContext;

/**
 * @author Evgeniy Lukovsky
 *
 */
public class BrightnessPlugin extends CordovaPlugin {



  public enum Action{
		setBrightness,
		getBrightness,
		setKeepScreenOn
	}

	private class SetTask implements Runnable{
		private Activity target = null;
		private LayoutParams lp = null;
		@Override
		public void run() {
			target.getWindow().setAttributes(lp);
		}
		public void setParams(Activity act, LayoutParams params){
			this.target = act;
			this.lp = params;
		}
	}

        private class KeepOnTask implements Runnable{
                private Window win = null;
                private boolean state = false;
                @Override
                public void run() {
                        if(state){
                                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else {
                                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                }
                public void setParams(Window win, boolean state){
                        this.win = win;
                        this.state = state;
                }
        }


	/* (non-Javadoc)
	 * @see org.apache.cordova.CordovaPlugin#execute(java.lang.String, org.json.JSONArray, org.apache.cordova.CallbackContext)
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		System.out.println("plugin has been started");
		boolean result = false;

		switch(Action.valueOf(action)){
		case setBrightness: result = true;
			setBrightness(args, callbackContext);
			break;
		case getBrightness: result = true;
			getBrightness(args, callbackContext);
			break;
		case setKeepScreenOn: result = true;
			setKeepScreenOn(args, callbackContext);
			break;
		}
		return result;
	}

	/**
	 * @param args
	 * @param callbackContext
	 * @return
	 */

  Context myContext;

  private boolean setBrightness(JSONArray args, CallbackContext callbackContext) {
	try {
		String value = args.getString(0);
		int brightness =  Integer.parseInt(value);

		ContentResolver resolver = this.cordova.getActivity().getContentResolver();
		Context ApplicationContext = this.cordova.getActivity().getApplicationContext();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.System.canWrite(ApplicationContext)) {

				Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
				Settings.System.putInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

			} else
			{

				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + ApplicationContext.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				cordova.getActivity().startActivity(intent);

			}

		}else{

			Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

		}

		callbackContext.success("OK");

		} catch (NullPointerException e) {
			System.out.println("Null pointer exception");
			System.out.println(e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("All went fine.");
		return true;
	}

  /**
	 * @param args
	 * @param callbackContext
	 * @return
	 */
	private boolean getBrightness(JSONArray args, CallbackContext callbackContext) {
		try {
			Activity activity = cordova.getActivity();
			WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
			Double brightness = (double) layoutParams.screenBrightness;
			callbackContext.success(brightness.toString());

		} catch (NullPointerException e) {
			System.out.println("Null pointer exception");
			System.out.println(e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
		System.out.println("All went fine.");
		return true;
	}
	/**
	 * @param args
	 * @param callbackContext
	 * @return
	 */
	private boolean setKeepScreenOn(JSONArray args, CallbackContext callbackContext){
		try {
			boolean value = args.getBoolean(0);
			Activity activity = cordova.getActivity();
			KeepOnTask task = new KeepOnTask();
                        task.setParams(activity.getWindow(), value);
                        activity.runOnUiThread(task);
			callbackContext.success("OK");

		} catch (NullPointerException e) {
			System.out.println("Null pointer exception");
			System.out.println(e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		} catch (JSONException e) {
			System.out.println("JSONException");
			System.out.println(e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
		System.out.println("All went fine.");
		return true;
	}
}
