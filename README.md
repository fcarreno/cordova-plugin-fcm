This is a fork of the plugin under [https://github.com/fechanique/cordova-plugin-fcm](https://github.com/fechanique/cordova-plugin-fcm)

With the following (Android only...):

* Added extra (launchedFromNotification: true) to the launch intent used when starting the app from a tapped notification.
   This can be used in combination with a plugin (e.g.: https://ionicframework.com/docs/native/web-intent/ ) to check for app intents/triggers during app start (e.g.: app.component for ionic apps) and redirect the user to a specific view (e.g.: message/notification details/data view), instead of navigating to the first/default view (e.g.: tabs) displayed during normal/manual app open.
   
   
* Updated `strings.xml` file path in ```/scripts/fcm_config_files_process.js```
(using fix suggested in this issue https://github.com/fechanique/cordova-plugin-fcm/issues/481#issuecomment-376008472)

* Added new action to FCMPlugin to allow consuming apps to dismiss a notification by its Id (remove from Android status bar)


# Installation:
```
$ ionic cordova plugin add @fcarreno/cordova-plugin-fcm
```
