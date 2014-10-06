package clowwindy.shadowvpn;

import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class VPN implements Runnable {
    ParcelFileDescriptor tunFd;
    String password;
    String server;
    int port;
    int mtu;
    int sockFd;
    Thread vpnThread;

    public VPN(ParcelFileDescriptor tunFd, String password, String server, int port,
               int mtu) {
        this.tunFd = tunFd;
        this.password = password;
        this.server = server;
        this.port = port;
        this.mtu = mtu;
    }

    @Override
    public void run() {
        System.err.println("Starting VPN");
        this.nativeRunVPN(tunFd.getFd(), password, server, port, mtu);
        System.err.println("VPN exited");
    }

    protected synchronized native int nativeRunVPN(int tunFd, String password, String server,
                                                   int port, int mtu);

    protected synchronized native int nativeStopVPN();

    protected synchronized native int nativeGetSockFd();

    public void startVPN() {
        if (vpnThread != null) {
            throw new RuntimeException("already running");
        }
        vpnThread = new Thread(this);
        vpnThread.start();
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100, 0);
            } catch (InterruptedException e) {
                // do nothing
            }
            if (0 != (sockFd = this.nativeGetSockFd())) {
                break;
            }
        }
    }

    public void stopVPN() {
        this.nativeStopVPN();
        try {
            tunFd.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    public int getSockFd() {
        return sockFd;
    }

    static {
        System.loadLibrary("vpn");
    }

}
