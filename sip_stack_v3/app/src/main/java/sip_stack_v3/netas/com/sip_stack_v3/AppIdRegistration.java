package sip_stack_v3.netas.com.sip_stack_v3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by ayildiz on 11/3/2015.
 */
public class AppIdRegistration {

    private static AppIdRegistration instance = new AppIdRegistration();

    private AppIdRegistration(){
    }

    public static AppIdRegistration getInstance() {
        return instance;
    }

    public MainActivity mAct;
    public Context mContext;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    String SENDER_ID = "935198058397"; //935198058397
    static final String TAG = "GCM Demo";
    GoogleCloudMessaging gcm;
    String regid;

    UserOper mUserOper = UserOper.getInstance();

    public void registerApp(){// eğer reg id yoksa gcme kaydol varsa olanı çek

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(mAct);

            regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            else {
                registerToEXPInBackground();
                Log.i("REGID", regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private boolean checkPlayServices() { // google playdeki id yei kontrol ediyoruz
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mAct);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mAct,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                mAct.finish();
            }
            return false;
        }
        return true;
    }

    private  String getRegistrationId(Context context){ //sender_id yi kaydettik
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID,"");

        if (registrationId.isEmpty()){
            Log.i(TAG,"Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGcmPreferences(Context context) { // sender_id ile ilgili shared preferances
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mAct.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {//apk versiyonunu gcm yollayıp uygun mu bakıyor
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {//eğer reg_id yoksa gcme kaydet
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("RESULT", msg);
                UserOper.getInstance().display("status", AppIdRegistration_Fragment.getInstance(), msg);
                registerToEXPInBackground();
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        Log.d("REGID", PROPERTY_REG_ID);
    } //?

    private void storeRegistrationId(Context context, String regId) { //?
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void registerToEXPInBackground() {//eğer reg_id varsa kaydetme

        Log.i("RESPONSE", "Sending...");

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = requestToWSwithConnection();
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("RESPONSE", msg);
                UserOper.getInstance().display("status", AppIdRegistration_Fragment.getInstance(), msg);
                UserOper.getInstance().display("status", AppIdRegistration_Fragment.getInstance(), "PN state on");

            }
        }.execute(null, null, null);
    }

    private String requestToWSwithConnection() { // soap requesti prova yolluyoruz veritabanına bizi kaydediyor
        String provIp = mUserOper.provip;
        String username = mUserOper.username;
        String domain = mUserOper.userdomain;

        final DatabaseOper dop = new DatabaseOper(mContext);
        final String sipinstance = dop.createSipInstance(dop);

        String token = getRegistrationId(mContext);//mActın contexti gelicek - String token = getRegistrationId(getApplicationContext());
        String body = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pus=\"pushnotification.ws.genband.com\">\n" +
                "<soapenv:Header>\n" +
                "<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                "<wsse:UsernameToken wsu:Id=\"UsernameToken-6\"><wsse:Username>admin</wsse:Username>\n" +
                "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">admin</wsse:Password>\n" +
                "<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">TSJDMN2R2LAfe4GbS/YyBA==</wsse:Nonce>\n" +
                "<wsu:Created>2015-03-10T12:38:54.321Z</wsu:Created></wsse:UsernameToken>\n" +
                "</wsse:Security>\n" +
                "</soapenv:Header>" +
                "   <soapenv:Body>\n" +
                "      <pus:subscribePushNotification soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <in0 xsi:type=\"com:UserNaturalKeyDO\" xmlns:com=\"common.ws.nortelnetworks.com\">\n" +
                "            <name xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"+username+"@"+domain+"</name>\n" +
                "         </in0>\n" +
                "         <in1 xsi:type=\"pus:PushNotificationSubscriptionDO\">\n" +
                "            <event xsi:type=\"xsd:int\">1</event>\n" +
                "            <opSys xsi:type=\"xsd:int\">1</opSys>\n" +
                "            <sipInstance xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">" + sipinstance +"</sipInstance>\n" +
                "            <sound xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">ringtone01</sound>\n" +
                "            <token xsi:type=\"soapenc:string\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"+token+"</token>\n" +
                "         </in1>\n" +
                "      </pus:subscribePushNotification>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        StringBuffer stringResponse = new StringBuffer();
        try {
            //provIp = ((EditText)findViewById(R.id.provIp)).getText().toString();
            HttpURLConnection connection;
            connection =  (HttpURLConnection)new URL("http://"+provIp+":8080/prov/services/PushNotificationAdminService").openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("SOAPAction", "");
            connection.setRequestProperty("Content-Type", "application/soap+xml;charset=utf-8");
            connection.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            //connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Length", "" + body.length());
            connection.setFixedLengthStreamingMode(body.length());
            connection.setRequestMethod("POST");
            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes("UTF-8"), 0, body.length());
            os.flush();
            os.close();
            int status = connection.getResponseCode();

            if(status==200)
            {

                BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()), 8 * 1024);
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    stringResponse.append(line);
                }
                Log.i("RESPONSE", "OK");
                return "Registered to EXP waiting for call";
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        dop.close();
        return stringResponse.toString();
    }

}
