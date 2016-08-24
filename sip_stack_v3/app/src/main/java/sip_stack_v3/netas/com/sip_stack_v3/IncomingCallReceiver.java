package sip_stack_v3.netas.com.sip_stack_v3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.internal.ca;

/**
 * Created by ayildiz on 10/22/2015.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private static IncomingCallReceiver instance = new IncomingCallReceiver();

    public IncomingCallReceiver(){}

    public static IncomingCallReceiver getInstance() {
        return instance;
    }

    private  SipAudioCall incomingCall = null;
    public  MainActivity mAct;
    private String peerProfile="";
    final UserOper mUserOper = UserOper.getInstance();

    public String RtoneFilePath = "/sdcard/Ringing_Tone.mp3";
    MediaPlayer mp = new MediaPlayer();

    Button btnAnswer;
    Button btnDecline;
    String username;
    String userdomain;

    public void onReceive(Context context, final Intent intent) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                final Calls_Fragment mCalls_Fragment = Calls_Fragment.getInstance();

                mUserOper.display("status",mCalls_Fragment,"");

                while (mCalls_Fragment.mView==null) {
                    ;
                }

                username = mUserOper.username;
                userdomain = mUserOper.userdomain;

                btnAnswer = (Button) mCalls_Fragment.mView.findViewById(R.id.btnAnswer);
                btnDecline = (Button) mCalls_Fragment.mView.findViewById(R.id.btnDecline);

                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            btnAnswer.setEnabled(true);
                            btnDecline.setEnabled(true);
                        }catch (Exception e){
                            mUserOper.display("log",mCalls_Fragment,"On receive set enabled error");
                        }
                    }
                });

                try {
                    SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                        @Override
                        public void onRinging(final SipAudioCall call, SipProfile caller) {
                            super.onRinging(call, caller);

                            mUserOper.display("log",mCalls_Fragment,"Registered with "+username+"@"+userdomain);
                            mUserOper.display("log",mCalls_Fragment,"PN state off");

                            try {
                                if (!mp.isPlaying()){
                                    mp.setDataSource(RtoneFilePath);
                                    mp.prepare();
                                    mp.start();
                                }
                            }catch (Exception e){
                                Log.e("Ringing tone error","error");
                                mUserOper.display("log",mCalls_Fragment,"Ringing tone error");
                            }

                            peerProfile = incomingCall.getPeerProfile().getUriString();
                            peerProfile = peerProfile.substring(4);
                        }

                        @Override
                        public void onCallEnded(SipAudioCall call){
                            super.onCallEnded(call);
                            try {
                                incomingCall.endCall();
                                incomingCall.close();

                                if (mp.isPlaying()){
                                    mp.reset();
                                }

                                mAct.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btnDecline.performClick();
                                    }
                                });

                                //mUserOper.display("log",mCalls_Fragment,"Unregistered with "+username+"@"+userdomain);

                            }catch (Exception e){
                                Log.e("Call ended","Error");
                                mUserOper.display("log",mCalls_Fragment,"Incoming call ended error");
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
                                mUserOper.display("log",mCalls_Fragment,"Incoming call on changed error");
                            }
                        }

                        @Override
                        public void onError(SipAudioCall call, int errorCode,
                                            String errorMessage) {
                            super.onError(call,errorCode,errorMessage);

                            if (errorCode==-2){
                                if (mp.isPlaying()){
                                    mp.reset();
                                }
                                mUserOper.unregisterWithSipStack(null);
                                mUserOper.display("log",mCalls_Fragment,"Unregistered with "+username+"@"+userdomain);
                            }
                            else {
                                Log.e("Call error "+errorCode+" "+errorMessage,"Error");
                            }

                            mUserOper.display("log",mCalls_Fragment,"Incoming call general error "+errorCode+" "+errorMessage);
                        }
                    };

                    incomingCall = mUserOper.mSipManager.takeAudioCall(intent, null);
                    incomingCall.setListener(listener, true);

                    Log.i("Call listener", "Incoming call");
                    mUserOper.display("status",mCalls_Fragment,"Call received from "+peerProfile);

                    mUserOper.currentCall = incomingCall; //we can hold incomingcall with hold button
                    mUserOper.peerProfile = peerProfile;

                } catch (Exception e) {
                    if (incomingCall != null) {
                        incomingCall.close();
                    }
                }
            }
        });
        thread.start();
    }
}
