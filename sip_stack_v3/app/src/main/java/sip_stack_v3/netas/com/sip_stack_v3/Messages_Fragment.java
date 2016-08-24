package sip_stack_v3.netas.com.sip_stack_v3;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ayildiz on 11/6/2015.
 */
public class Messages_Fragment extends android.support.v4.app.Fragment {

    private static Messages_Fragment instance =new Messages_Fragment();

    public Messages_Fragment(){}

    public static Messages_Fragment getInstance() {return instance;}

    UserOper mUserOper = UserOper.getInstance();
    IncomingImReceiver mIncomingImReceiver = IncomingImReceiver.getInstance();

    Button btnReceive;
    Button btnClear;

    View mView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.messages_layout,container,false);

        final android.support.v4.app.Fragment fragment = this;

        btnReceive  = (Button) mView.findViewById(R.id.btnReceive);

        btnClear = (Button) mView.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserOper.clearall(fragment);
            }
        });

        return mView;

    }
}
