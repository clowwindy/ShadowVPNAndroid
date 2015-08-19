package org.shadowvpn.shadowvpn.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;
import org.shadowvpn.shadowvpn.service.ShadowVPNService;
import org.shadowvpn.shadowvpn.service.ShadowVPNService.ShadowVPNServiceBinder;
import org.shadowvpn.shadowvpn.ui.fragment.ShadowVPNListFragment;
import org.shadowvpn.shadowvpn.ui.fragment.ShadowVPNListFragment.IOnFragmentInteractionListener;
import org.shadowvpn.shadowvpn.util.Intents;
import org.shadowvpn.shadowvpn.util.ShadowVPNConfigureHelper;

public class MainActivity extends ActionBarActivity implements IOnFragmentInteractionListener, ServiceConnection
{
	private static final int REQUEST_CODE_VPN_PREPARE = 1;

	private ShadowVPNService mShadowVPNService;

	private ShadowVPNConfigure mCurrentSelectedShadowVPNConfigure;

	@Override
	protected void onCreate(final Bundle pSavedInstanceState)
	{
		super.onCreate(pSavedInstanceState);

		this.setContentView(R.layout.activity_main);

		if (pSavedInstanceState == null)
		{
			this.getSupportFragmentManager().beginTransaction().add(R.id.container, ShadowVPNListFragment.newInstance()).commit();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		final Intent intent = new Intent(this, ShadowVPNService.class);
		this.startService(intent);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		final Intent intent = new Intent(this, ShadowVPNService.class);
		this.bindService(intent, this, Context.BIND_ABOVE_CLIENT);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		this.unbindService(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu)
	{
		this.getMenuInflater().inflate(R.menu.activity_main, pMenu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pMenuItem)
	{
		switch (pMenuItem.getItemId())
		{
			case R.id.menu_vpn_add:
				Intents.addShadowVPNConfigure(this);
				return true;
			default:
				return super.onOptionsItemSelected(pMenuItem);
		}
	}

	@Override
	public void onShadowVPNConfigureClick(final ShadowVPNConfigure pShadowVPNConfigure)
	{
		this.mCurrentSelectedShadowVPNConfigure = pShadowVPNConfigure;

		if (!this.mCurrentSelectedShadowVPNConfigure.isSelected())
		{
			this.prepareShadowVPN();
		}
	}

	@Override
	public void onShadowVPNConfigureStop(final ShadowVPNConfigure pShadowVPNConfigure)
	{
		if (this.mShadowVPNService != null)
		{
			this.mShadowVPNService.stopVPN();
		}
	}

	@Override
	public void onShadowVPNConfigureEdit(final ShadowVPNConfigure pShadowVPNConfigure)
	{
		Intents.editShadowVPNConfigure(this, pShadowVPNConfigure);
	}

	@Override
	public void onShadowVPNConfigureDelete(final ShadowVPNConfigure pShadowVPNConfigure)
	{
		ShadowVPNConfigureHelper.delete(this, pShadowVPNConfigure.getTitle());
	}

	@Override
	protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData)
	{
		super.onActivityResult(pRequestCode, pResultCode, pData);

		if (pRequestCode == MainActivity.REQUEST_CODE_VPN_PREPARE && pResultCode == MainActivity.RESULT_OK)
		{
			this.startShadowVPN();
		}
	}

	public void prepareShadowVPN()
	{
		final Intent intent = ShadowVPNService.prepare(this);

		if (intent != null)
		{
			this.startActivityForResult(intent, MainActivity.REQUEST_CODE_VPN_PREPARE);
		}
		else
		{
			this.startShadowVPN();
		}
	}

	private void startShadowVPN()
	{
		if (this.mCurrentSelectedShadowVPNConfigure != null)
		{
			final Intent intent = new Intent(this, ShadowVPNService.class);
			intent.putExtra(ShadowVPNService.EXTRA_VPN_TITLE, this.mCurrentSelectedShadowVPNConfigure.getTitle());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_SERVER_IP, this.mCurrentSelectedShadowVPNConfigure.getServerIP());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_PORT, this.mCurrentSelectedShadowVPNConfigure.getPort());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_PASSWORD, this.mCurrentSelectedShadowVPNConfigure.getPassword());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_USER_TOKEN, this.mCurrentSelectedShadowVPNConfigure.getUserToken());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_LOCAL_IP, this.mCurrentSelectedShadowVPNConfigure.getLocalIP());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_MAXIMUM_TRANSMISSION_UNITS, this.mCurrentSelectedShadowVPNConfigure.getMaximumTransmissionUnits());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_CONCURRENCY, this.mCurrentSelectedShadowVPNConfigure.getConcurrency());
			intent.putExtra(ShadowVPNService.EXTRA_VPN_BYPASS_CHINA_ROUTES, this.mCurrentSelectedShadowVPNConfigure.isBypassChinaRoutes());

			this.startService(intent);
			this.bindService(intent, this, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	public void onServiceConnected(final ComponentName pComponentName, final IBinder pIBinder)
	{
		final ShadowVPNServiceBinder binder = (ShadowVPNServiceBinder) pIBinder;

		this.mShadowVPNService = binder.getShadowVPNService();
	}

	@Override
	public void onServiceDisconnected(final ComponentName pComponentName)
	{
		this.mShadowVPNService = null;
	}
}