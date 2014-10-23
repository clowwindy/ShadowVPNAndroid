package org.shadowvpn.shadowvpn.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.ui.fragment.ShadowVPNConfigureEditFragment;

public class ShadowVPNConfigureEditActivity extends ActionBarActivity
{
	public static final String EXTRA_TITLE = "extra_title";

	@Override
	protected void onCreate(final Bundle pSavedInstanceState)
	{
		super.onCreate(pSavedInstanceState);

		this.setContentView(R.layout.activity_shadow_vpn_configure_edit);

		final Bundle extras = this.getIntent().getExtras();

		if (pSavedInstanceState == null)
		{
			if (extras == null)
			{
				final Fragment editFragment = ShadowVPNConfigureEditFragment.newInstance();

				this.getSupportFragmentManager().beginTransaction().add(R.id.container, editFragment).commit();
			}
			else
			{
				final String title = extras.getString(ShadowVPNConfigureEditActivity.EXTRA_TITLE);

				final Fragment editFragment = ShadowVPNConfigureEditFragment.newInstance(title);

				this.getSupportFragmentManager().beginTransaction().add(R.id.container, editFragment).commit();
			}
		}
	}
}