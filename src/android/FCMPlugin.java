package com.gae.scaffolder.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Map;
//import java.util.List;

public class FCMPlugin extends CordovaPlugin {

	private static final String TAG = "FCMPlugin";

	public static CordovaWebView gWebView;
	public static String notificationCallBack = "FCMPlugin.onNotificationReceived";
	public static String tokenRefreshCallBack = "FCMPlugin.onTokenRefreshReceived";
	public static Boolean notificationCallBackReady = false;
  //private static List<Map<String, Object>> pendingPushes = new ArrayList<Map<String,Object>>();
  public static Map<String, Object> lastPush = null;


	public FCMPlugin() {}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		gWebView = webView;
		Log.d(TAG, "==> FCMPlugin initialize");
		FirebaseMessaging.getInstance().subscribeToTopic("android");
		FirebaseMessaging.getInstance().subscribeToTopic("all");
	}

	public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

		Log.d(TAG,"==> FCMPlugin execute: "+ action);

		try{
			// READY //
			if (action.equals("ready")) {
				//
				callbackContext.success();
			}
			// GET TOKEN //
			else if (action.equals("getToken")) {
				cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						try{
							String token = FirebaseInstanceId.getInstance().getToken();
							callbackContext.success( FirebaseInstanceId.getInstance().getToken() );
							Log.d(TAG,"\tToken: "+ token);
						}catch(Exception e){
							Log.d(TAG,"\tError retrieving token");
						}
					}
				});
			}
			// NOTIFICATION CALLBACK REGISTER //
			else if (action.equals("registerNotification")) {
				notificationCallBackReady = true;
				cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
            if(lastPush != null) FCMPlugin.sendPushPayload( lastPush );
            lastPush = null;
            //if(!pendingPushes.isEmpty()) FCMPlugin.processPendingPushes();
            //pendingPushes.clear();
						callbackContext.success();
					}
				});
			}
			// UN/SUBSCRIBE TOPICS //
			else if (action.equals("subscribeToTopic")) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{
							FirebaseMessaging.getInstance().subscribeToTopic( args.getString(0) );
							callbackContext.success();
						}catch(Exception e){
							callbackContext.error(e.getMessage());
						}
					}
				});
			}
			else if (action.equals("unsubscribeFromTopic")) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{
							FirebaseMessaging.getInstance().unsubscribeFromTopic( args.getString(0) );
							callbackContext.success();
						}catch(Exception e){
							callbackContext.error(e.getMessage());
						}
					}
				});
			}
      else if (action.equals("dismissNotification")) {
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            try{
              Log.d(TAG, "FCMPlugin dismissNotificaton called for notification Id: " + args.getString(0)); //notificationId
              NotificationManager notificationManager =   (NotificationManager) cordova.getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
              notificationManager.cancel(args.getInt(0));
              callbackContext.success();
            }catch(Exception e){
              callbackContext.error(e.getMessage());
            }
          }
        });
      }
			else{
				callbackContext.error("Method not found");
				return false;
			}
		}catch(Exception e){
			Log.d(TAG, "ERROR: onPluginAction: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
		return true;
	}

	public static void sendPushPayload(Map<String, Object> payload) {
		Log.d(TAG, "==> FCMPlugin sendPushPayload");
		Log.d(TAG, "\tnotificationCallBackReady: " + notificationCallBackReady);
		Log.d(TAG, "\tgWebView: " + gWebView);
	    try {
		    JSONObject jo = new JSONObject();
        for (String key : payload.keySet()) {
            jo.put(key, payload.get(key));
            Log.d(TAG, "\tpayload: " + key + " => " + payload.get(key));
        }

      String callBack = "javascript:" + notificationCallBack + "(" + jo.toString() + ")";
			if(notificationCallBackReady && gWebView != null){
				Log.d(TAG, "\tSent PUSH to view: " + callBack);
        gWebView.sendJavascript(callBack);
				// TODO: implement Plugin result, as suggested in the .sendJavascript deprecated documentation.
        // PluginResult dataResult = new PluginResult(PluginResult.Status.OK);
        // dataResult.setKeepCallback(true);
        // savedCallbackContext.sendPluginResult(dataResult);

        // Maybe have a static callbackContent property for notifications, setting it on registerNotification success above (execute method)?
        // Check for similar example of callbackContext saved from execute (on IntentShim), under registerBroadcastReceiver case inside execute...
			}else {
          Log.d(TAG, "\tView not ready. SAVED NOTIFICATION: " + callBack);
          lastPush = payload;
          //pendingPushes.add(payload);
			}
		} catch (Exception e) {
        Log.d(TAG, "\tERROR sendPushToView. SAVED NOTIFICATION: " + e.getMessage());
        lastPush = payload;
        //pendingPushes.add(payload);
		}
	}

  /*public static void processPendingPushes() {

	  Log.d(TAG, "==> FCMPlugin processPendingPushes (" + Integer.toString(pendingPushes.size()) + " pushes pending...)" );
	  for(Map<String, Object> pendingPush: pendingPushes)
      FCMPlugin.sendPushPayload(pendingPush);

    Log.d(TAG, "==> FCMPlugin processPendingPushes (finished processing!" );
  }*/

	public static void sendTokenRefresh(String token) {
		Log.d(TAG, "==> FCMPlugin sendRefreshToken");
	  try {
			String callBack = "javascript:" + tokenRefreshCallBack + "('" + token + "')";
			gWebView.sendJavascript(callBack);
		} catch (Exception e) {
			Log.d(TAG, "\tERROR sendRefreshToken: " + e.getMessage());
		}
	}

  @Override
	public void onDestroy() {
		gWebView = null;
		notificationCallBackReady = false;
		lastPush = null;
		//pendingPushes.clear();
	}
}
