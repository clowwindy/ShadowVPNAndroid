package org.shadowvpn.shadowvpn.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;
import org.shadowvpn.shadowvpn.util.ShadowVPNConfigureHelper;

public class ShadowVPNConfigureEditFragment extends Fragment
{
	private static final String KEY_TITLE = "key_title";

	public static ShadowVPNConfigureEditFragment newInstance()
	{
		return ShadowVPNConfigureEditFragment.newInstance(null);
	}

	public static ShadowVPNConfigureEditFragment newInstance(final String pTitle)
	{
		final ShadowVPNConfigureEditFragment fragment = new ShadowVPNConfigureEditFragment();

		final Bundle arguments = new Bundle();
		arguments.putString(ShadowVPNConfigureEditFragment.KEY_TITLE, pTitle);
		fragment.setArguments(arguments);

		return fragment;
	}

	private String mTitle;

	private FloatingLabelEditText mTitleText;

	private FloatingLabelEditText mServerIPText;

	private FloatingLabelEditText mPortText;

	private FloatingLabelEditText mPasswordText;

	private FloatingLabelEditText mLocalIPText;

	private FloatingLabelEditText mMaximumTransmissionUnitsText;

	private SwitchCompat mBypassChinaRoutesSwitch;

	@Override
	public void onCreate(final Bundle pSavedInstanceState)
	{
		super.onCreate(pSavedInstanceState);

		this.setHasOptionsMenu(true);

		if (this.getArguments() != null)
		{
			this.mTitle = this.getArguments().getString(ShadowVPNConfigureEditFragment.KEY_TITLE);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater pLayoutInflater, final ViewGroup pContainer, final Bundle pSavedInstanceState)
	{
		final View view = pLayoutInflater.inflate(R.layout.fragment_shadow_vpn_configure_edit, pContainer, false);

		this.mTitleText = (FloatingLabelEditText) view.findViewById(R.id.text_title);
		this.mServerIPText = (FloatingLabelEditText) view.findViewById(R.id.text_server_ip);
		this.mPortText = (FloatingLabelEditText) view.findViewById(R.id.text_port);
		this.mPasswordText = (FloatingLabelEditText) view.findViewById(R.id.text_password);
		this.mLocalIPText = (FloatingLabelEditText) view.findViewById(R.id.text_local_ip);
		this.mMaximumTransmissionUnitsText = (FloatingLabelEditText) view.findViewById(R.id.text_maximum_transmission_units);
		this.mBypassChinaRoutesSwitch = (SwitchCompat) view.findViewById(R.id.switch_bypass_china_routes);

		if (TextUtils.isEmpty(this.mTitle))
		{
			this.mPortText.setInputWidgetText(String.valueOf(0));
			this.mLocalIPText.setInputWidgetText(ShadowVPNConfigureHelper.DEFAULT_LOCAL_IP);
			this.mMaximumTransmissionUnitsText.setInputWidgetText(String.valueOf(ShadowVPNConfigureHelper.DEFAULT_MAXIMUM_TRANSMISSION_UNITS));
		}
		else
		{
			final ShadowVPNConfigure configure = ShadowVPNConfigureHelper.exists(this.getActivity(), this.mTitle);

			this.mTitleText.setInputWidgetText(configure.getTitle());
			this.mServerIPText.setInputWidgetText(configure.getServerIP());
			this.mPortText.setInputWidgetText(String.valueOf(configure.getPort()));
			this.mPasswordText.setInputWidgetText(configure.getPassword());
			this.mLocalIPText.setInputWidgetText(configure.getLocalIP());
			this.mMaximumTransmissionUnitsText.setInputWidgetText(String.valueOf(configure.getMaximumTransmissionUnits()));
			this.mBypassChinaRoutesSwitch.setChecked(configure.isBypassChinaRoutes());
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(final Menu pMenu, final MenuInflater pMenuInflater)
	{
		pMenuInflater.inflate(R.menu.fragment_shadow_vpnconfigure_edit, pMenu);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu pMenu)
	{
		pMenu.findItem(R.id.menu_delete).setVisible(!TextUtils.isEmpty(this.mTitle));

		super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pMenuItem)
	{
		switch (pMenuItem.getItemId())
		{
			case android.R.id.home:
				if (TextUtils.isEmpty(this.mTitle))
				{
					if (this.createShadowVPNConfigure())
					{
						this.getActivity().finish();
					}
				}
				else
				{
					if (this.updateShadowVPNConfigure())
					{
						this.getActivity().finish();
					}
				}
				return true;
			case R.id.menu_discard:
				this.getActivity().finish();
				return true;
			case R.id.menu_delete:
				ShadowVPNConfigureHelper.delete(this.getActivity(), this.mTitle);
				this.getActivity().finish();
				return true;
			default:
				return super.onOptionsItemSelected(pMenuItem);
		}
	}

	private boolean createShadowVPNConfigure()
	{
		final boolean inputResult = this.checkInput();

		if (inputResult)
		{
			final boolean existsResult = this.checkConfigureExists();

			if (!existsResult)
			{
				final String title = this.mTitleText.getInputWidgetText().toString();
				final String serverIP = this.mServerIPText.getInputWidgetText().toString();
				final int port = Integer.parseInt(this.mPortText.getInputWidgetText().toString());
				final String password = this.mPasswordText.getInputWidgetText().toString();
				final String localIP = this.mLocalIPText.getInputWidgetText().toString();
				final int maximumTransmissionUnits = Integer.parseInt(this.mMaximumTransmissionUnitsText.getInputWidgetText().toString());
				final boolean bypassChinaRoutes = this.mBypassChinaRoutesSwitch.isChecked();

				ShadowVPNConfigureHelper.create(this.getActivity(), title, serverIP, port, password, localIP, maximumTransmissionUnits, bypassChinaRoutes);

				return true;
			}
		}

		return false;
	}

	private boolean updateShadowVPNConfigure()
	{
		final boolean inputResult = this.checkInput();

		if (inputResult)
		{
			final ShadowVPNConfigure shadowVPNConfigure = ShadowVPNConfigureHelper.exists(this.getActivity(), this.mTitle);

			final String title = this.mTitleText.getInputWidgetText().toString();
			final String serverIP = this.mServerIPText.getInputWidgetText().toString();
			final int port = Integer.parseInt(this.mPortText.getInputWidgetText().toString());
			final String password = this.mPasswordText.getInputWidgetText().toString();
			final String localIP = this.mLocalIPText.getInputWidgetText().toString();
			final int maximumTransmissionUnits = Integer.parseInt(this.mMaximumTransmissionUnitsText.getInputWidgetText().toString());
			final boolean bypassChinaRoutes = this.mBypassChinaRoutesSwitch.isChecked();

			ShadowVPNConfigureHelper.update(this.getActivity(), shadowVPNConfigure, title, serverIP, port, password, localIP, maximumTransmissionUnits, bypassChinaRoutes, shadowVPNConfigure.isSelected());
		}

		return inputResult;
	}

	private boolean checkInput()
	{
		if (TextUtils.isEmpty(this.mTitleText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_title_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		if (TextUtils.isEmpty(this.mServerIPText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_server_ip_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		if (TextUtils.isEmpty(this.mPortText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_port_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		if (TextUtils.isEmpty(this.mPasswordText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_password_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		if (TextUtils.isEmpty(this.mLocalIPText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_local_ip_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		if (TextUtils.isEmpty(this.mMaximumTransmissionUnitsText.getInputWidgetText().toString()))
		{
			Toast.makeText(this.getActivity(), R.string.toast_vpn_configure_maximum_transmission_units_null, Toast.LENGTH_SHORT).show();

			return false;
		}

		return true;
	}

	private boolean checkConfigureExists()
	{
		final String title = this.mTitleText.getInputWidgetText().toString();

		final ShadowVPNConfigure configure = ShadowVPNConfigureHelper.exists(this.getActivity(), title);

		if (configure != null)
		{
			Toast.makeText(this.getActivity(), this.getString(R.string.toast_vpn_configure_exists, title), Toast.LENGTH_SHORT).show();

			return true;
		}
		else
		{
			return false;
		}
	}
}