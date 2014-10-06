package clowwindy.shadowvpn;

/**
 * Created by clowwindy on 10/6/14.
 */
public class VPN implements Runnable {
    int tunFd;
    int sockFd;
    Thread vpnThread;

    public VPN(int tunFd) {
        this.tunFd = tunFd;
    }

    @Override
    public void run() {
        this.runVPN();
    }

    public native int runVPN();

    public native int stopVPN();

    public void startVPN() throws Exception {
        if (vpnThread != null) {
            throw new Exception("already running");
        }
        vpnThread = new Thread(this);
        vpnThread.start();
    }

    static {
        System.loadLibrary("vpn");
    }

}
