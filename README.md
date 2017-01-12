# Capstone-Project-Stage2
1. Please create an app on Reddit (https://www.reddit.com/prefs/apps), generate Client id, and redirect url
and put that value in following class's variable.

com.app.reddit.base.AppConstants

 public static String CLIENT_ID = ""/*Add your client id*/;
 
 public static String REDIRECT_URI=""/*Add redirect url*/;

2. Add keystore file in root directory and write keystore related information in "keystore.properties" file, then you app can be builds and deployed using the installRelease Gradle task.