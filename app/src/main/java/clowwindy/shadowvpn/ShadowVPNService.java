package clowwindy.shadowvpn;

import android.app.PendingIntent;
import android.net.VpnService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class ShadowVPNService extends VpnService {

    static private VPN vpn;
    static private boolean isRunning;
    private Messenger messenger;
    ParcelFileDescriptor tunFd;

    public static final String VPN_SERVER = "server";
    public static final String VPN_PORT = "port";
    public static final String VPN_PASSWORD = "password";
    public static final String VPN_LOCAL_IP = "local_ip";
    public static final String VPN_MTU = "mtu";

    public static boolean isRunning() {
        return isRunning;
    }

    public ShadowVPNService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO prevent starting twice
        if (isRunning) {
            return START_STICKY;
        }
        isRunning = true;
        Bundle extras = intent.getExtras();

        if (extras != null) {
            messenger = (Messenger)extras.get(SettingsActivity.HANDLER);

        }
        Intent mainIntent = new Intent(this.getApplicationContext(), SettingsActivity.class);
        PendingIntent pendIntent =
                PendingIntent.getActivity(this.getApplicationContext(), 0, mainIntent, 0);
        try {
            tunFd = new Builder()
                    .addAddress(extras.getString(VPN_LOCAL_IP), 24)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("8.8.4.4")
                    .setConfigureIntent(pendIntent)
                    .setSession("ShadowVPN")
                    .establish();
        } catch (RuntimeException e) {
            System.err.println(e);
            return START_STICKY;
        }
        try {
            vpn = new VPN(tunFd, extras.getString(VPN_PASSWORD),
                    extras.getString(VPN_SERVER), extras.getInt(VPN_PORT),
                    extras.getInt(VPN_MTU));
        } catch (IOException e) {
            System.err.println(e);
            // TODO show the error to the user
            this.stopSelf();
        }
        if (!this.protect(vpn.getSockFd())) {
            throw new RuntimeException("can not protect socket");
        }
        vpn.startVPN();
                Message msg = Message.obtain();
        try {
            messenger.send(msg);
        } catch (android.os.RemoteException e1) {
            // do nothing
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isRunning = false;
        Message msg = Message.obtain();
        try {
            messenger.send(msg);
        } catch (android.os.RemoteException e1) {
            // do nothing
        }
        try {
            tunFd.close();
        } catch (Exception e) {
            // do nothing
            System.err.println(e);
        }
        vpn.stopVPN();
        this.stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        stopSelf();
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
