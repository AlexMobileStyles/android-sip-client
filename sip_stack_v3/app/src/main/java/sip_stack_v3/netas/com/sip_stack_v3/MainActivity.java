package sip_stack_v3.netas.com.sip_stack_v3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import java.net.DatagramSocket;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    AppIdRegistration mAppIdRegistration = AppIdRegistration.getInstance();
    IncomingImReceiver mIncomingImReceiver = IncomingImReceiver.getInstance();
    IncomingCallReceiver mIncomingCallReceiver = IncomingCallReceiver.getInstance();
    GcmIntentService mGcmIntentService = GcmIntentService.getInstance();

    Context mContext=this;
    MainActivity mAct=this;
    View mView;

    UserOper mUseroper = UserOper.getInstance();

    static final String DISPLAY_MESSAGE_ACTION =
            "sip_stack_v3.netas.com.sip_stack_v3.DISPLAY_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIncomingImReceiver.mContext = mContext;

        UserOper.getInstance().init(this);

        mIncomingCallReceiver.mAct = this;
        mIncomingImReceiver.mAct = this;
        mGcmIntentService.mAct = this;
        mAppIdRegistration.mAct = this;
        mAppIdRegistration.mContext = this.getApplicationContext();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("sip_stack_v3.netas.com.sip_stack_v3.INCOMING_CALL");
        this.registerReceiver(mIncomingCallReceiver, filter);
        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mIncomingCallReceiver);
        unregisterReceiver(mHandleMessageReceiver);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment mFragment = null;

        switch (position){
            case 0:
                mFragment = Registration_Fragment.getInstance();
                break;
            case 1:
                mFragment = Calls_Fragment.getInstance();
                break;
            case 2:
                mFragment = Messages_Fragment.getInstance();
                break;
            case 3:
                mFragment = AppIdRegistration_Fragment.getInstance();
                break;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.registration_layout, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (GcmIntentService.msgType) {
                        case "1":
                            mAct.onNavigationDrawerItemSelected(1);
                            mUseroper.registerWithSipStack(null);
                            break;
                        case "9":
                            mAct.onNavigationDrawerItemSelected(2);
                            mIncomingImReceiver.ImReceiver();
                            break;
                        default:
                            break;
                    }
                }
            };

}

