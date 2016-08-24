package sip_stack_v3.netas.com.sip_stack_v3;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by ayildiz on 11/6/2015.
 */
public class AppIdRegistration_Fragment extends android.support.v4.app.Fragment {

    private static AppIdRegistration_Fragment instance = new AppIdRegistration_Fragment();

    public AppIdRegistration_Fragment(){}

    public static AppIdRegistration_Fragment getInstance() {
        return instance;
    }

    View mView;
    AppIdRegistration mAppIdRegistration = AppIdRegistration.getInstance();
    UserOper mUserOper = UserOper.getInstance();

    Button btnRgstr;
    Button btnClear;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        instance=this;
        mView = inflater.inflate(R.layout.appidregistration_layout,container,false);
        final android.support.v4.app.Fragment fragment = this;

        btnRgstr = (Button) mView.findViewById(R.id.btnRgstr);
        btnRgstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppIdRegistration.registerApp();
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
}
