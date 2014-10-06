package clowwindy.shadowvpn;

import android.app.PendingIntent;
import android.net.VpnService;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

public class MyVPNService extends VpnService {

    private ParcelFileDescriptor tunFd;

    public MyVPNService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent mainIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent pendIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, mainIntent, 0);
        tunFd = new Builder().addAddress("10.7.0.2", 24).addDnsServer("8.8.8.8").setConfigureIntent(pendIntent).setSession("ShadowVPN").establish();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
    }
    static {

    }
}
