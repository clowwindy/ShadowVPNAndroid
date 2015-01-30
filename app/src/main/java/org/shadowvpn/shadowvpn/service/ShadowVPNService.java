package org.shadowvpn.shadowvpn.service;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.ShadowVPN;
import org.shadowvpn.shadowvpn.util.ShadowVPNConfigureHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShadowVPNService extends VpnService
{
	public static final String EXTRA_VPN_TITLE = "extra_vpn_title";

	public static final String EXTRA_VPN_SERVER_IP = "extra_vpn_server_ip";

	public static final String EXTRA_VPN_PORT = "extra_vpn_port";

	public static final String EXTRA_VPN_PASSWORD = "extra_vpn_password";

	public static final String EXTRA_VPN_LOCAL_IP = "extra_vpn_local_ip";

	public static final String EXTRA_VPN_MAXIMUM_TRANSMISSION_UNITS = "extra_vpn_maximum_transmission_units";

	public static final String EXTRA_VPN_CONCURRENCY = "extra_vpn_concurrency";

	public static final String EXTRA_VPN_BYPASS_CHINA_ROUTES = "extra_vpn_bypass_china_routes";

	private final IBinder mBinder = new ShadowVPNServiceBinder();

	private volatile Looper mServiceLooper;

	private volatile ServiceHandler mServiceHandler;

	private ShadowVPN mShadowVPN;

	private final class ServiceHandler extends Handler
	{
		public ServiceHandler(final Looper pLooper)
		{
			super(pLooper);
		}

		@Override
		public void handleMessage(final Message pMessage)
		{
			ShadowVPNService.this.onHandleIntent((Intent) pMessage.obj);

			ShadowVPNService.this.stopSelf(pMessage.arg1);
		}
	}

	public final class ShadowVPNServiceBinder extends Binder
	{
		public ShadowVPNService getShadowVPNService()
		{
			return ShadowVPNService.this;
		}

		@Override
		protected boolean onTransact(final int pCode, final Parcel pData, final Parcel pReply, final int pFlags) throws RemoteException
		{
			if (pCode == IBinder.LAST_CALL_TRANSACTION)
			{
				ShadowVPNService.this.onRevoke();

				return true;
			}

			return super.onTransact(pCode, pData, pReply, pFlags);
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		final HandlerThread thread = new HandlerThread("ShadowVPNService");
		thread.start();

		this.mServiceLooper = thread.getLooper();
		this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
	}

	@Override
	public void onStart(final Intent pIntent, final int pStartId)
	{
		final Message message = this.mServiceHandler.obtainMessage();
		message.obj = pIntent;
		message.arg1 = pStartId;

		this.mServiceHandler.sendMessage(message);
	}

	@Override
	public int onStartCommand(Intent pIntent, int pFlags, int pStartId)
	{
		this.onStart(pIntent, pStartId);

		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(final Intent pIntent)
	{
		return this.mBinder;
	}

	protected void onHandleIntent(final Intent pIntent)
	{
		final Bundle extras = pIntent.getExtras();

		if (extras == null)
		{
			return;
		}

		this.stopVPN();

		final String title = extras.getString(ShadowVPNService.EXTRA_VPN_TITLE);
		final String serverIP = extras.getString(ShadowVPNService.EXTRA_VPN_SERVER_IP);
		final int port = extras.getInt(ShadowVPNService.EXTRA_VPN_PORT);
		final String password = extras.getString(ShadowVPNService.EXTRA_VPN_PASSWORD);
		final String localIP = extras.getString(ShadowVPNService.EXTRA_VPN_LOCAL_IP);
		final int maximumTransmissionUnits = extras.getInt(ShadowVPNService.EXTRA_VPN_MAXIMUM_TRANSMISSION_UNITS);
		final int concurrency = extras.getInt(ShadowVPNService.EXTRA_VPN_CONCURRENCY);
		final boolean bypassChinaRoutes = extras.getBoolean(ShadowVPNService.EXTRA_VPN_BYPASS_CHINA_ROUTES);

		final Builder builder = new Builder();
		builder.addAddress(localIP, 24);
		this.setupShadowVPNRoute(builder, bypassChinaRoutes);
		builder.addDnsServer("8.8.8.8");
		builder.addDnsServer("8.8.4.4");
		builder.setSession(this.getString(R.string.app_name) + "[" + title + "]");

		final ParcelFileDescriptor fileDescriptor = builder.establish();

		if (fileDescriptor == null)
		{
			return;
		}

		this.mShadowVPN = new ShadowVPN(fileDescriptor, password, serverIP, port, maximumTransmissionUnits, concurrency);

		try
		{
			this.mShadowVPN.init();
		}
		catch (final IOException pIOException)
		{
			Log.e(ShadowVPNService.class.getSimpleName(), "", pIOException);
		}

		this.protect(this.mShadowVPN.getSockFileDescriptor());

		ShadowVPNConfigureHelper.selectShadowVPNConfigure(this, title);

		this.mShadowVPN.start();
	}

	private void setupShadowVPNRoute(final Builder pBuilder, final boolean pBypassChinaRoutes)
	{
		if (pBypassChinaRoutes)
		{
			final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.foreign)));

			String line;

			try
			{
				while ((line = reader.readLine()) != null)
				{
					final String[] route = line.split("/");

					if (route.length == 2)
					{
						pBuilder.addRoute(route[0], Integer.parseInt(route[1]));
					}
				}
			}
			catch (final Throwable pThrowable)
			{
				Log.e(ShadowVPNService.class.getSimpleName(), "", pThrowable);
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (final IOException pIOException)
				{
					// do nothing
				}
			}
		}
		else
		{
			pBuilder.addRoute("0.0.0.0", 0);
		}
	}

	@Override
	public void onDestroy()
	{
		this.mServiceLooper.quit();

		ShadowVPNConfigureHelper.resetAllSelectedValue(this);

		this.stopVPN();
	}

	@Override
	public void onRevoke()
	{
		super.onRevoke();

		this.stopVPN();
	}

	public boolean isShadowVPNRunning()
	{
		return this.mShadowVPN != null && this.mShadowVPN.isRunning();
	}

	public void stopVPN()
	{
		if (this.mShadowVPN != null && this.mShadowVPN.isRunning())
		{
			this.mShadowVPN.shouldStop();
		}
	}
}