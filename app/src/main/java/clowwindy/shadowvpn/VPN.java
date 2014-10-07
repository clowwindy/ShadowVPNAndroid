package clowwindy.shadowvpn;

import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class VPN implements Runnable {
    ParcelFileDescriptor tunFd;
    String password;
    String server;
    int port;
    int mtu;
    Thread vpnThread;

    public VPN(ParcelFileDescriptor tunFd, String password, String server, int port,
               int mtu) throws IOException {
        this.tunFd = tunFd;
        this.password = password;
        this.server = server;
        this.port = port;
        this.mtu = mtu;
        int r = this.nativeInitVPN(tunFd.getFd(), password, server, port, mtu);
        if (r != 0) {
            throw new IOException("can not create VPN");
        }
    }

    @Override
    public void run() {
        System.err.println("Starting VPN");
        int r = this.nativeRunVPN();
        // TODO stop service when exit abnormally
        System.err.println(r);
        System.err.println("VPN exited");
    }

    protected native int nativeInitVPN(int tunFd, String password, String server,
                                                   int port, int mtu);

    protected native int nativeRunVPN();

    protected native int nativeStopVPN();

    protected native int nativeGetSockFd();

    public void startVPN() {
        if (vpnThread != null) {
            throw new RuntimeException("already running");
        }
        vpnThread = new Thread(this);
        vpnThread.start();
    }

    public void stopVPN() {
        this.nativeStopVPN();
        try {
            vpnThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        try {
            tunFd.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    public int getSockFd() {
        return nativeGetSockFd();
    }

    static {
        System.loadLibrary("vpn");
    }

}
