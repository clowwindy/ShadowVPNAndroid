package org.shadowvpn.shadowvpn;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class ShadowVPN
{
	private static final int DEFAULT_MAXIMUM_TRANSMISSION_UNITS = 1440;

	private static final int DEFAULT_CONCURRENCY = 1;

	private final ParcelFileDescriptor mTUNFileDescriptor;

	private final String mPassword;
	
	private final String mUserToken;

	private final String mServer;

	private final int mPort;

	private final int mMaximumTransmissionUnits;

	private final int mConcurrency;

	private boolean mIsRunning;

	public ShadowVPN(final ParcelFileDescriptor pTUNFileDescriptor, final String pPassword, final String pUserToken, final String pServer, final int pPort)
	{
		this(pTUNFileDescriptor, pPassword, pUserToken, pServer, pPort, ShadowVPN.DEFAULT_MAXIMUM_TRANSMISSION_UNITS, ShadowVPN.DEFAULT_CONCURRENCY);
	}

	public ShadowVPN(final ParcelFileDescriptor pTUNFileDescriptor, final String pPassword, final String pUserToken, final String pServer, final int pPort, final int pMaximumTransmissionUnits, final int pConcurrency)
	{
		this.mTUNFileDescriptor = pTUNFileDescriptor;

		this.mPassword = pPassword;

		this.mUserToken = pUserToken;

		this.mServer = pServer;

		this.mPort = pPort;

		this.mMaximumTransmissionUnits = pMaximumTransmissionUnits;

		this.mConcurrency = pConcurrency;
	}

	public void init() throws IOException
	{
		if (this.nativeInitVPN(this.mTUNFileDescriptor.getFd(), this.mPassword, this.mUserToken, this.mServer, this.mPort, this.mMaximumTransmissionUnits, this.mConcurrency) != 0)
		{
			throw new IOException("Failed to create ShadowVPN");
		}
	}

	public void start()
	{
		if (this.mIsRunning)
		{
			return;
		}

		this.mIsRunning = true;

		this.nativeRunVPN();

		this.mIsRunning = false;
	}

	public void shouldStop()
	{
		this.mIsRunning = false;

		this.nativeStopVPN();

		try
		{
			this.mTUNFileDescriptor.close();
		}
		catch (final IOException pIOException)
		{
			Log.e(ShadowVPN.class.getSimpleName(), "", pIOException);
		}
	}

	public boolean isRunning()
	{
		return this.mIsRunning;
	}

	public int getSockFileDescriptor()
	{
		return this.nativeGetSockFd();
	}

	protected native int nativeInitVPN(final int pTUNFileDescriptor, final String pPassword, final String pUserToken, final String pServer, final int pPort, final int pMaximumTransmissionUnits, final int pConcurrency);

	protected native int nativeRunVPN();

	protected native int nativeStopVPN();

	protected native int nativeGetSockFd();

	static
	{
		System.loadLibrary("vpn");
	}
}