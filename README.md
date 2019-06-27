This is a fork of the plugin under [https://github.com/fechanique/cordova-plugin-fcm](https://github.com/fechanique/cordova-plugin-fcm)

With the following (Android only...):

* Added extra (launchedFromNotification: true) to the launch intent used when starting the app from a tapped notification.
   This can be used in combination with a plugin (e.g.: https://ionicframework.com/docs/native/web-intent/ ) to check for app intents/triggers during app start (e.g.: app.component for ionic apps) and redirect the user to a specific view (e.g.: message/notification details/data view), instead of navigating to the first/default view (e.g.: tabs) displayed during normal/manual app open.
   
   
* Updated `strings.xml` file path in ```/scripts/fcm_config_files_process.js```
(using fix suggested in this issue https://github.com/fechanique/cordova-plugin-fcm/issues/481#issuecomment-376008472)

* Added new action to FCMPlugin to allow consuming apps to dismiss a notification by its Id (remove from Android status bar)

* Added support to send push payload -when app is open- based on a flag received as part of data/extras.


#### Installation:
```
$ ionic cordova plugin add cordova-plugin-fcm-updated
```

#### Note:
Please check [installation instructions](https://github.com/fechanique/cordova-plugin-fcm#installation) of original plugin for pre-requisites, prior to proceeding with installation command above.

In addition, in order to use feature that allows dismissing a notification, please use this updated plugin wrapper (instead of default one provided by ionic): https://www.npmjs.com/package/@fcarreno/ionic-native-fcm
