package sip_stack_v3.netas.com.sip_stack_v3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.support.annotation.Nullable;

/**
 * Created by ayildiz on 11/6/2015.
 */

public class Calls_Fragment extends android.support.v4.app.Fragment {

    private static Calls_Fragment instance =new Calls_Fragment();

    public Calls_Fragment(){}

    public static Calls_Fragment getInstance() {return instance;}

    View mView;

    UserOper mUserOper = UserOper.getInstance();

    Button btnMakeCall;
    Button btnEndCall;
    Button btnHoldCall;
    Button btnClear;
    Button btnAnswer;
    Button btnDecline;

    boolean btnMakeCallState=true;
    boolean btnEndState=false;
    boolean btnHoldCallState=false;
    boolean btnAnswerState=false;
    boolean btnDeclineState=false;

    String calltousername;
    String calltouserdom;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        instance=this;
        mView = inflater.inflate(R.layout.calls_layout,container,false);

        final android.support.v4.app.Fragment fragment = this;

        btnMakeCall  = (Button) mView.findViewById(R.id.btnMakeCall);
        btnEndCall = (Button) mView.findViewById(R.id.btnEndCall);
        btnHoldCall = (Button) mView.findViewById(R.id.btnHoldCall);
        btnAnswer = (Button) mView.findViewById(R.id.btnAnswer);
        btnDecline = (Button) mView.findViewById(R.id.btnDecline);

        btnMakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calltousername = ((EditText)mView.findViewById(R.id.calltousername)).getText().toString();
                calltouserdom = ((EditText)mView.findViewById(R.id.calltouserdom)).getText().toString();

                mUserOper.setInfoForCall(calltousername,calltouserdom);

                mUserOper.makecallWithSipStack(fragment);

                btnMakeCall.setEnabled(false);
                btnEndCall.setEnabled(true);
            }
        });

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calltousername = ((EditText)mView.findViewById(R.id.calltousername)).getText().toString();
                calltouserdom = ((EditText)mView.findViewById(R.id.calltouserdom)).getText().toString();

                mUserOper.setInfoForCall(calltousername,calltouserdom);
                mUserOper.endcallWithSipStack(fragment);

                btnMakeCall.setEnabled(true);
                btnEndCall.setEnabled(false);
                btnHoldCall.setEnabled(false);
            }
        });

        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserOper.answercallWithSipStack(fragment);

                btnAnswer.setEnabled(false);
                btnDecline.setEnabled(true);
                btnHoldCall.setEnabled(true);
            }
        });

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserOper.declinecallWithSipStack(fragment);

                btnDecline.setEnabled(false);
                btnAnswer.setEnabled(false);
                btnHoldCall.setEnabled(false);
            }
        });


        btnHoldCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnHoldCall.getText().equals("Hold Call")){
                    btnHoldCall.setText("Unhold Call");
                }
                else {
                    btnHoldCall.setText("Hold Call");
                }
                mUserOper.holdcallWithSipStack(fragment);
            }

        });

        btnClear = (Button) mView.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserOper.clearall(fragment);
            }
        });
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        btnMakeCallState = btnMakeCall.isEnabled();
        btnEndState = btnEndCall.isEnabled();
        btnHoldCallState = btnHoldCall.isEnabled();
        btnAnswerState = btnAnswer.isEnabled();
        btnDeclineState = btnDecline.isEnabled();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        btnMakeCallState = btnMakeCall.isEnabled();
        btnEndState = btnEndCall.isEnabled();
        btnHoldCallState = btnHoldCall.isEnabled();
        btnAnswerState = btnAnswer.isEnabled();
        btnDeclineState = btnDecline.isEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        btnMakeCall.setEnabled(btnMakeCallState);
        btnEndCall.setEnabled(btnEndState);
        btnHoldCall.setEnabled(btnHoldCallState);
        btnAnswer.setEnabled(btnAnswerState);
        btnDecline.setEnabled(btnDeclineState);
    }
}
