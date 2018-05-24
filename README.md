- Fork of https://github.com/fechanique/cordova-plugin-fcm with the following changes (applicable to Android):

-- Added extra (launchedFromNotification: true) to the launch intent used when starting the app from a tapped notification.
   This can be used in combination with a plugin (e.g.: https://ionicframework.com/docs/native/web-intent/ ) that checks for app intents/triggers during app start (e.g.: app.component for ionic apps) and redirect the user to a specific view (e.g.: message/notification details/data view), instead of navigating to the first/default view (e.g.: tabs) displayed during normal/manual app open.
   
   
-- Updated strings.xml file path in /scripts/fcm_config_files_process.js 
(using fix suggested in this issue https://github.com/fechanique/cordova-plugin-fcm/issues/481#issuecomment-376008472)

-- Updated plugin logic to save multiple pushes (in case multiple ones are received and the app/view is not yet ready)
E.g.: app is being started up, which may take a couple of seconds, and multiple messages could be received during that time...

