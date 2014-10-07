package clowwindy.shadowvpn;

import android.app.PendingIntent;
import android.net.VpnService;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class ShadowVPNService extends VpnService {

    static private VPN vpn;

    public ShadowVPNService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO prevent starting twice
        Intent mainIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent pendIntent =
                PendingIntent.getActivity(this.getApplicationContext(), 0, mainIntent, 0);
        ParcelFileDescriptor tunFd = new Builder()
                .addAddress("10.7.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setConfigureIntent(pendIntent)
                .setSession("ShadowVPN")
                .establish();
        try {
            vpn = new VPN(tunFd, "my_password", "10.0.1.109", 1123, 1440);
        } catch (IOException e) {
            System.err.println(e);
            // TODO show the error to the user
            this.stopSelf();
        }
        if (!this.protect(vpn.getSockFd())) {
            throw new RuntimeException("can not protect socket");
        }
        vpn.startVPN();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onRevoke() {
        vpn.stopVPN();
        super.onRevoke();
    }
}
