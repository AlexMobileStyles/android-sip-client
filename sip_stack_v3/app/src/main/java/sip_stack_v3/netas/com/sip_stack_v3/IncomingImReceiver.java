package sip_stack_v3.netas.com.sip_stack_v3;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ayildiz on 11/19/2015.
 */
public class IncomingImReceiver extends Activity {

    private static IncomingImReceiver instance = new IncomingImReceiver();

    private IncomingImReceiver(){}

    public static IncomingImReceiver getInstance() {
        return instance;
    }

    final UserOper mUseroper = UserOper.getInstance();
    final Messages_Fragment mMessages_Fragment = Messages_Fragment.getInstance();

    public  MainActivity mAct;

    Context mContext;
    String username;
    String userdomain;
    String serverIp;
    String serverPort;
    Boolean imregisterstate;

    DatagramSocket ds = null;

    boolean stopListening = false;

    private Object stoplisteninglock = new Object();

    public void setstopListening(boolean stopListening){
        synchronized(stoplisteninglock) {
            this.stopListening = stopListening;
        }
    }

    public boolean getstopListening(){
        synchronized (stoplisteninglock) {
            return stopListening;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);
    }

    public void ImReceiver() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... dss) {

                username = mUseroper.username;
                userdomain = mUseroper.userdomain;
                serverIp = mUseroper.userserverip;
                serverPort = mUseroper.userserverport;
                final String ip = getLocalIpAddress();

                final DatabaseOper dop = new DatabaseOper(mContext);
                final String sipinstance = dop.createSipInstance(dop);

                try {
                    ds = new DatagramSocket();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                String regMsg = "REGISTER sip:" + userdomain + ":" + serverPort + " SIP/2.0\n" +
                        "From: <sip:" + username + "@" + userdomain + ">;tag=3614207402-602969\n" +
                        "To: <sip:" + username + "@" + userdomain + ">\n" +
                        "CSeq: 1 REGISTER\n" +
                        "Call-ID: 5114-3614207402-602946@domain1.com\n" + //buradaki domain1 ipsi sesm ipsi ile değişsin mi
                        "Via: SIP/2.0/UDP " + ip + ":" + ds.getLocalPort() + ";branch=z9hG4bKf7379dbc026a5e9419bf53633e73d25c\n" +
                        "Content-Length: 0\n" +
                        "Contact: <sip:" + username + "@" + ip + ":" + ds.getLocalPort() + ">;+sip.instance=\"<urn:uuid:" + sipinstance + ">\"\n" +
                        "User-Agent:  LaLaCall-a/2.0.3\n" +
                        "Expires: 900\n" +
                        "Max-Forwards: 69\n" +
                        "Allow: CANCEL\n" +
                        "Allow: ACK\n" +
                        "Allow: INVITE\n" +
                        "Allow: BYE\n" +
                        "Allow: OPTIONS\n" +
                        "Allow: REGISTER\n" +
                        "Allow: NOTIFY\n" +
                        "Allow: INFO\n" +
                        "Allow: REFER\n" +
                        "Allow: SUBSCRIBE\n" +
                        "Allow: UPDATE\n" +
                        "Allow: MESSAGE\n" +
                        "Allow: PUBLISH\n\n";

                try {
                    DatagramPacket dp;
                    dp = new DatagramPacket(regMsg.getBytes(), regMsg.getBytes().length, InetAddress.getByName(serverIp), Integer.parseInt(serverPort));
                    ds.setBroadcast(true);
                    ds.send(dp);
                    String cseq = "1", callid = "", from = "", via = "", to = "";
                    boolean answered = false;
                    while (true) {
                        byte[] receivedata2 = new byte[4096];
                        dp = new DatagramPacket(receivedata2, receivedata2.length);
                        ds.receive(dp);
                        byte[] b = dp.getData();
                        String s = new String(b);

                        if (s.contains("SIP/2.0 200")) {
                            answered = true;
                            if (b.length > 0) {
                                mUseroper.display("log",mMessages_Fragment,"Registered with "+username+"@"+userdomain);
                                mUseroper.display("log",mMessages_Fragment,"PN State off");
                                imregisterstate = true;
                            }
                        }

                        if (s.contains("MESSAGE sip:")) {
                            if (!answered) {
                                mUseroper.display("log",mMessages_Fragment,"SIP: Message Received.");
                            }

                            final String from1 = s.split("From:")[1].split("\n")[0].trim().trim().split("@")[0].split(":")[1];

                            mUseroper.display("log",mMessages_Fragment,"SIP IM received from " + from1);

                            if(s.contains("xanilx_")) {

                                final String mxsgbody = s.split("xanilx_")[1];
                                mUseroper.display("status",mMessages_Fragment,""+ mxsgbody);
                            }

                            cseq = s.split("CSeq:")[1].split("\n")[0].trim();
                            callid = s.split("Call-ID:")[1].split("\n")[0].trim();
                            from = s.split("From:")[1].split("\n")[0].trim();
                            via = s.split("Via:")[1].split("\n")[0].trim();
                            to = s.split("To:")[1].split("\n")[0].trim();

                            send200(from, to, callid, cseq, via);
                            break;
                        }
                    }
                    unregisterFromEXP();
                    //imregisterstate = false;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dop.close();
                return "ok";

            }

        }.execute(null, null, null);

    }


    public void send200(String tfrom, String tto, String tcallid, String tcseq, String tvia) {

        final String ip = getLocalIpAddress();
        try {
            String le = "v=0\n" +
                    "o="+tto+" 548541 0 IN IP4 "+ip+"\n" +
                    "s=nortelnetworks\n" +
                    "e=unknown@invalid.net\n" +
                    "c=IN IP4 "+ip+"\n" +
                    "t=0 0\n" +
                    "m=audio 50014 RTP/AVP 0 110\n" +
                    "c=IN IP4 "+ip+"\n" +
                    "a=rtpmap:0 PCMU/8000\n" +
                    "a=rtpmap:110 telephone-event/8000\n" +
                    "a=ptime:20\n" +
                    "m=video 0 RTP/AVP 34\n" +
                    "c=IN IP4 "+ip+"\n" +
                    "b=AS:66\n" +
                    "b=TIAS:65536\n" +
                    "a=rtpmap:34 H263/90000\n" +
                    "a=fmtp:34 QCIF=3;SQCIF=3;CIF=3;F=1\n" +
                    "a=maxprate:8.00\n\n";

            String ok2 = "SIP/2.0 200 OK\n" +
                    "From:  " + tfrom + "\n" +
                    "To: " + tto + ";tag=12345\n" +
                    "CSeq: " + tcseq + "\n" +
                    "Call-ID: " + tcallid + "\n" +
                    "Via: "+tvia+"\n" +
                    //"Content-Length: " + le.getBytes().length + "\n" +
                    "Content-Length: 0\n" +
                    "Content-Type: application/sdp\n" +
                    "Contact: <sip:" + username + "@"+ip+":" + ds.getLocalPort() + ">\n" +
                    "User-Agent:  A2PC 10.2.1161\n" +
                    "Supported: replaces\n" +
                    "Supported: timer\n" +
                    "Supported: from-change\n" +
                            /*
                            "Supported: replaces,timer\n"+
                            "x-nt-party-id: swauser01;r/\n"+
                             */
                    "Allow: ACK\n" +
                    "Allow: MESSAGE\n" +
                    "Allow: NOTIFY\n" +
                    "Allow: INVITE\n" +
                    "Allow: BYE\n" +
                    "Allow: CANCEL\n" +
                    "Allow: REFER\n" +
                    "Allow: OPTIONS\n" +
                    "Allow: INFO\n" +
                    "Allow: PUBLISH\n" +
                    "Allow: UPDATE\n" +
                    "Session-Expires: 1800;refresher=uas\n\n";
            DatagramPacket dp2 = new DatagramPacket(ok2.getBytes(), ok2.getBytes().length, InetAddress.getByName(serverIp), Integer.parseInt(serverPort));
            ds.send(dp2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void unregisterFromEXP() {

        try {
            final DatabaseOper dop = new DatabaseOper(mContext);
            final String sipinstance = dop.createSipInstance(dop);

            //setstopListening(true);

            username = mUseroper.username;
            userdomain = mUseroper.userdomain;
            serverIp = mUseroper.userserverip;
            serverPort = mUseroper.userserverport;
            final String ip = getLocalIpAddress();

            try {
                ds = new DatagramSocket();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //Thread.sleep(3000);

            String unregMsg = "REGISTER sip:" + userdomain + ":" + serverPort + " SIP/2.0\n" +
                    "From: <sip:" + username + "@" + userdomain + ">;tag=3614207402-602969\n" +
                    "To: <sip:" + username + "@" + userdomain + ">\n" +
                    "CSeq: 1 REGISTER\n" +
                    "Call-ID: 5114-3614207402-602946@domain1.com\n" +
                    "Via: SIP/2.0/UDP " + ip + ":" + ds.getLocalPort() + ";branch=z9hG4bKf7379dbc026a5e9419bf53633e73d25c\n" +
                    "Content-Length: 0\n" +
                    "Contact: <sip:" + username + "@" + ip + ":" + ds.getLocalPort() + ">;+sip.instance=\"<urn:uuid:" + sipinstance + ">\"\n" +
                    "User-Agent:  LaLaCall-a/2.0.3\n" +
                    "Expires: 0\n" +
                    "Max-Forwards: 69\n" +
                    "Allow: CANCEL\n" +
                    "Allow: ACK\n" +
                    "Allow: INVITE\n" +
                    "Allow: BYE\n" +
                    "Allow: OPTIONS\n" +
                    "Allow: REGISTER\n" +
                    "Allow: NOTIFY\n" +
                    "Allow: INFO\n" +
                    "Allow: REFER\n" +
                    "Allow: SUBSCRIBE\n" +
                    "Allow: UPDATE\n" +
                    "Allow: MESSAGE\n" +
                    "Allow: PUBLISH\n\n";
            DatagramPacket dp = new DatagramPacket(unregMsg.getBytes(), unregMsg.length(), InetAddress.getByName(serverIp), Integer.parseInt(serverPort));
            ds.send(dp);
            dop.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mUseroper.display("log",mMessages_Fragment,"Unregistered with "+username+"@"+userdomain);
            mUseroper.display("log",mMessages_Fragment,"PN State on");

            if (ds != null) {
                ds.close();
            }
        }
    }

    public String getLocalIpAddress() {

        WifiManager wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        return ip;

    }

}
