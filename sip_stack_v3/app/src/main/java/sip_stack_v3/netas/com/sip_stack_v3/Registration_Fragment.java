package sip_stack_v3.netas.com.sip_stack_v3;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by ayildiz on 11/6/2015.
 */
public class Registration_Fragment extends android.support.v4.app.Fragment {

    private static Registration_Fragment instance = new Registration_Fragment();

    public Registration_Fragment(){
    }

    public static Registration_Fragment getInstance() {
        return instance;
    }

    View mView;
    String username;
    String domain;
    String userpw;
    String serverIp;
    String serverPort;
    String provip;

    UserOper mUserOper = UserOper.getInstance();

    Button btnRgstr;
    Button btnUnrgstr;
    Button btnClear;

    boolean btnRgstrState=true;
    boolean btnUnrgstrState=false;

    IncomingImReceiver mIncomingImReceiver = IncomingImReceiver.getInstance();

    boolean isFirstRun=true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.registration_layout,container,false);
        final android.support.v4.app.Fragment fragment = this;

        username = ((EditText)mView.findViewById(R.id.sipUser)).getText().toString();
        domain = ((EditText)mView.findViewById(R.id.sipDomain)).getText().toString();
        userpw = ((EditText)mView.findViewById(R.id.sipPw)).getText().toString();
        serverIp = ((EditText)mView.findViewById(R.id.sipServerIp)).getText().toString();
        serverPort = ((EditText)mView.findViewById(R.id.sipServerPort)).getText().toString();
        provip = ((EditText)mView.findViewById(R.id.provIp)).getText().toString();

        mUserOper.setInfoForReg(username,domain,userpw,serverIp,serverPort,provip);

        btnRgstr = (Button) mView.findViewById(R.id.btnRgstr);
        btnUnrgstr = (Button) mView.findViewById(R.id.btnUnrgstr);

        if (isFirstRun)
        {
            switch (GcmIntentService.msgType) {
                case "1":
                    mUserOper.registerWithSipStack(null);
                    break;
                case "9":
                    mIncomingImReceiver.ImReceiver();
                    break;
                default:
                    break;
            }
            isFirstRun = false;
        }

        btnRgstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRgstr.setEnabled(false);
                btnUnrgstr.setEnabled(true);

                username = ((EditText)mView.findViewById(R.id.sipUser)).getText().toString();
                domain = ((EditText)mView.findViewById(R.id.sipDomain)).getText().toString();
                userpw = ((EditText)mView.findViewById(R.id.sipPw)).getText().toString();
                serverIp = ((EditText)mView.findViewById(R.id.sipServerIp)).getText().toString();
                serverPort = ((EditText)mView.findViewById(R.id.sipServerPort)).getText().toString();
                provip = ((EditText)mView.findViewById(R.id.provIp)).getText().toString();

                mUserOper.setInfoForReg(username,domain,userpw,serverIp,serverPort,provip);

                mUserOper.registerWithSipStack(fragment);
            }
        });

        btnUnrgstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRgstr.setEnabled(true);
                btnUnrgstr.setEnabled(false);

                username = ((EditText)mView.findViewById(R.id.sipUser)).getText().toString();
                domain = ((EditText)mView.findViewById(R.id.sipDomain)).getText().toString();
                userpw = ((EditText)mView.findViewById(R.id.sipPw)).getText().toString();
                serverIp = ((EditText)mView.findViewById(R.id.sipServerIp)).getText().toString();
                serverPort = ((EditText)mView.findViewById(R.id.sipServerPort)).getText().toString();
                provip = ((EditText)mView.findViewById(R.id.provIp)).getText().toString();

                mUserOper.setInfoForReg(username,domain,userpw,serverIp,serverPort,provip);

                mUserOper.unregisterWithSipStack(fragment);
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
        btnRgstrState = btnRgstr.isEnabled();
        btnUnrgstrState = btnUnrgstr.isEnabled();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        btnRgstrState = btnRgstr.isEnabled();
        btnUnrgstrState = btnUnrgstr.isEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
            btnRgstr.setEnabled(btnRgstrState);
            btnUnrgstr.setEnabled(btnUnrgstrState);
    }
}
