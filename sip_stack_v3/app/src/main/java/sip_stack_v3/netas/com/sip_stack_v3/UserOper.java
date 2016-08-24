package sip_stack_v3.netas.com.sip_stack_v3;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ayildiz on 11/3/2015.
 */
public class UserOper {

    private static UserOper instance = new UserOper();

    private UserOper(){
    }

    public static UserOper getInstance() {
        return instance;
    }

    public String username;
    public String userdomain;
    public String userpw;
    public String userserverip;
    public String userserverport;
    public String calltousername;
    public String calltouserdomain;
    public String provip;

    public void setInfoForReg(String tname,String tdomain,String tpw,String tserverip,String tserverport,String tprovip){
        username = tname;
        userdomain =tdomain;
        userpw = tpw;
        userserverip = tserverip;
        userserverport = tserverport;
        provip = tprovip;
    }

    public void setInfoForCall(String tcallusername,String tcalluserdomain){
        calltousername = tcallusername;
        calltouserdomain = tcalluserdomain;
    }

    public SipManager mSipManager;
    public SipProfile mSipProfile;
    public SipAudioCall currentCall;

    public String peerProfile=""; //Profile of PCC side.
    public String localProfile=""; //Profile of android side.
    public String fragment_name="";

    public String CtoneFilePath = "/sdcard/Calling_Tone.mp3";
    private MediaPlayer mp = new MediaPlayer();

    public Button btnMakeCall;
    public Button btnEndCall;
    public Button btnHoldCall;

    public Boolean isRegistered=false;

    public MainActivity mAct;

    public void registerWithSipStack(final android.support.v4.app.Fragment fragment) {

        if (!isRegistered) {
            try {

                SipProfile.Builder builder = new SipProfile.Builder(username, userdomain);
                builder.setPassword(userpw);
                builder.setOutboundProxy(userserverip);
                builder.setPort(Integer.parseInt(userserverport));
                builder.setProtocol("UDP");
                mSipProfile = builder.build();

                localProfile = mSipProfile.getUriString();
                localProfile = localProfile.substring(4);

                Intent i = new Intent();
                i.setAction("sip_stack_v3.netas.com.sip_stack_v3.INCOMING_CALL");
                PendingIntent pi = PendingIntent.getBroadcast(mAct, 0, i, Intent.FILL_IN_DATA);
                mSipManager.open(mSipProfile, pi, null);

                Log.e("SipProfile Builder","SipProfile building successful");

            } catch (Exception e) {
                Log.e("SipProfile builder", "Error");
                display("log",fragment, "SipProfile builder error");
            }

            try {
                SipRegistrationListener listener = new SipRegistrationListener() {
                    //Registering with local profile
                    @Override
                    public void onRegistering(String localProfileUri) {
                        Log.i("SIP registration", "Registering.");
                        display("log",fragment, "Registering.....");
                    }

                    @Override
                    public void onRegistrationDone(String localProfileUri, long expiryTime) {
                        Log.i("Sip registration", "Done!");
                        display("log",fragment, "Registered with " + localProfile);
                        isRegistered=true;
                    }

                    @Override
                    public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                        Log.e("SIP registration", "Failed! URI: " + localProfileUri + " Reason: " + errorMessage);
                        display("log",fragment, "Failed on registration with " + localProfile + " Reason: " + errorMessage);
                    }
                };

                try {
                    mSipManager.register(mSipProfile, 3600, listener);
                } catch (SipException e) {
                    Log.e("Error", "SipManager registration error");
                    display("log",fragment, "SipManager registration error");
                }

            } catch (Exception e) {
                Log.e("Error", "SipListener registration");
                display("log",fragment, "SipListener registration error");
            }
        }
        else {
            display("log",fragment,"You already registered with "+localProfile);
        }
    }

    public void unregisterWithSipStack(final android.support.v4.app.Fragment fragment) {

        try {
            if (mSipProfile != null) {

                SipRegistrationListener listener = new SipRegistrationListener() {
                    @Override
                    public void onRegistering(String localProfileUri) {
                        Log.i("SIP unregistration", "Unregistering.");
                        display("log",fragment, "Unregistering.....");
                    }

                    @Override
                    public void onRegistrationDone(String localProfileUri, long expiryTime) {
                        Log.i("SIP unregistration","Done!");
                        display("log",fragment, "Unregistered with "+ localProfile);
                        mSipProfile=null;
                        closeLocalProfile();
                        isRegistered=false;
                    }

                    @Override
                    public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                        Log.e("SIP Unregistration", "Failed! URI: " + localProfile + " Reason: " + errorMessage);
                        display("log",fragment, "Failed on unregistration with " + localProfile + " Reason: " + errorMessage);
                    }
                };
                try {
                    mSipManager.unregister(mSipProfile,listener);
                }catch (SipException e){
                    Log.e("Error","SipManager unregistration error");
                    display("log", fragment, "SipManager unregistration error");
                }
            }
            else {
                display("log",fragment,"First you must be registered");
            }

        } catch (Exception e) {
            Log.e("Error","SipListener unregistration error");
            display("log",fragment, "SipListener unregistration error");
        }
    }

    public void makecallWithSipStack(final android.support.v4.app.Fragment fragment) {

        final Calls_Fragment mCalls_Fragment = Calls_Fragment.getInstance();

        btnEndCall = (Button) mCalls_Fragment.mView.findViewById(R.id.btnEndCall);
        btnMakeCall = (Button) mCalls_Fragment.mView.findViewById(R.id.btnMakeCall);
        btnHoldCall = (Button) mCalls_Fragment.mView.findViewById(R.id.btnHoldCall);

        SipAudioCall.Listener listener = new SipAudioCall.Listener() {

            @Override
            public void onCallEstablished(SipAudioCall call) {
                super.onCallEstablished(call);

                try {
                    call.startAudio();
                    call.setSpeakerMode(false);

                    display("status",fragment, "Call established with " + peerProfile);
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnHoldCall.setEnabled(true);
                        }
                    });

                }catch (Exception e){
                    Log.e("Make Call","Error");
                    display("log",fragment, "Failed on making call to " + peerProfile);
                }
            }

            @Override
            public void onCalling(SipAudioCall call) {
                peerProfile = currentCall.getPeerProfile().getUriString();
                peerProfile = peerProfile.substring(4);

                display("status",fragment, "Calling "+peerProfile);
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                super.onCallEnded(call);

                try {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnEndCall.performClick();
                        }
                    });

                    call.endCall();
                    call.close();

                }catch (Exception e){
                    Log.e("Make call","Error");
                    display("log",fragment, "Failed on ending call with " + peerProfile);
                }
            }

            @Override
            public void onRingingBack(SipAudioCall call) {
                super.onRingingBack(call);
                try {
                    if (!mp.isPlaying()){
                        mp.setDataSource(CtoneFilePath);
                        mp.prepare();
                        mp.start();
                    }
                }catch (Exception e){
                    Log.e("Media player","error");
                    display("log",fragment, "onRingingBack State error");
                }
            }

            @Override
            public void onChanged(SipAudioCall call) {

                super.onChanged(call);

                try {
                    if (mp.isPlaying()){
                        mp.reset();
                    }
                }catch (Exception e){
                    Log.e("Call on changed","error");
                    display("log",fragment, "On changed state error");
                }
            }

            @Override
            public void onError(SipAudioCall call, int errorCode,
                                String errorMessage) {
                super.onError(call, errorCode, errorMessage);
                /*
                In basic call and android is orig side case; before the call establishment if pcc declines the call,
                at the android side we get -2 error code.
                 */
                if (errorCode==-2){

                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnEndCall.performClick();
                        }
                    });
                    if (mp.isPlaying()){
                        mp.reset();
                    }
                }
                else {
                    Log.e("Call error "+errorCode+" "+errorMessage,"error");
                    display("log", fragment, "Call error " + errorCode + " " + errorMessage);
                }
            }
        };
        try {
            if (mSipManager != null && mSipProfile !=null)
            {
                currentCall = mSipManager.makeAudioCall(mSipProfile.getUriString(), calltousername+
                        "@"+calltouserdomain , listener, 30);
            }
        }catch (Exception e){
            Log.e("Error","Failed on making call to \"+peerProfile");
            display("log", fragment, "Failed on making call to " + peerProfile);
        }
    }

    public void answercallWithSipStack(android.support.v4.app.Fragment fragment){
        try {
            currentCall.answerCall(3600);
            currentCall.startAudio();
            display("status",fragment, "Call established with " + peerProfile);
        }catch (Exception e){
            Log.e("Incoming call answer", "Error");
            display("log",fragment, "Incoming call answer error");
        }
    }

    public void declinecallWithSipStack(android.support.v4.app.Fragment fragment){
        try {
            currentCall.endCall();
            currentCall.close();
            unregisterWithSipStack(null);
            if (mp.isPlaying()){
                mp.reset();
            }
            display("status",fragment,"Call ended with "+peerProfile);
            display("log",fragment,"Unregistered with "+localProfile);
            display("log",fragment,"PN state on");
        }catch (Exception e){
            Log.e("Call decline","Error");
            display("log",fragment, "Incoming call decline error");
        }
    }

    public void holdcallWithSipStack(android.support.v4.app.Fragment fragment){
        try {
            if (currentCall.isOnHold()){
                currentCall.continueCall(3600);
                display("status",fragment,"Call continues with "+ peerProfile);
            }
            else {
                currentCall.holdCall(3600);
                display("status",fragment,"Call holding with "+ peerProfile);
            }
        }catch (Exception e){
            display("log",fragment,"call holding exception");
        }
    }

    public void endcallWithSipStack(android.support.v4.app.Fragment fragment){
        try {
            if (mp.isPlaying()){
                mp.reset();
            }
            if (mSipProfile!=null){
                currentCall.endCall();
                currentCall.close();
                display("status",fragment, "Call ended with " + peerProfile);
            }
        } catch (SipException e) {
            display("status",fragment, "Failed ending call with "+ peerProfile);
        }
    }

    public void display(final String type,final android.support.v4.app.Fragment fragment, final String msg){
        if (fragment!=null){

            fragment_name = fragment.toString().split("_")[0];

            switch (fragment_name) {
                case "Registration":
                    mAct.onNavigationDrawerItemSelected(0);
                    break;
                case "Calls":
                    mAct.onNavigationDrawerItemSelected(1);
                    break;
                case "Messages":
                    mAct.onNavigationDrawerItemSelected(2);
                    break;
                case "AppIdRegistration":
                    mAct.onNavigationDrawerItemSelected(3);
                    break;
                default:
                    break;
            }

            mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!msg.equals("") && type.equals("status")){
                        ((TextView)fragment.getView().findViewById(R.id.displayStatus)).append("\n" + msg);
                    }
                    else if (!msg.equals("") && type.equals("log")){
                        ((TextView)fragment.getView().findViewById(R.id.displayLog)).append("\n" + msg);
                    }
                }catch (Exception e){
                    Log.d("UserOper display method","Exception");
                }

            }
        });
        }
    }

    public void clearall(final android.support.v4.app.Fragment fragment){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(fragment.getView().findViewById(R.id.displayLog)!=null)
                    {
                        ((TextView)fragment.getView().findViewById(R.id.displayLog)).setText("");
                    }
                    if(fragment.getView().findViewById(R.id.displayStatus)!=null){
                        ((TextView)fragment.getView().findViewById(R.id.displayStatus)).setText("");
                    }
                }catch (Exception e){
                    Log.d("Exception","UserOper clearall method");
                }
            }
        });
    }

    public void init(MainActivity act) {
        mAct = act;

        if (mSipManager == null){
            mSipManager  = SipManager.newInstance(act);
        }
    }

    public void closeLocalProfile() {
        if (mSipManager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                mSipManager.close(mSipProfile.getUriString());
            }
        } catch (Exception e) {
            Log.d("Error", "UserOper Close local profile", e);
        }
    }
}
