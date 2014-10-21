package org.shadowvpn.shadowvpn.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;
import org.shadowvpn.shadowvpn.util.ShadowVPNConfigureHelper;

public class ShadowVPNConfigureEditFragment extends Fragment
{
	private static final String KEY_TITLE = "key_title";

	private static final String KEY_SERVER_IP = "key_server_ip";

	private static final String KEY_PORT = "key_port";

	private static final String KEY_PASSWORD = "key_password";

	private static final String KEY_LOCAL_IP = "key_local_ip";

	private static final String KEY_MAXIMUM_TRANSMISSION_UNITS = "key_maximum_transmission_units";

	public static ShadowVPNConfigureEditFragment newInstance(final String pTitle, final String pServerIP, final int pPort, final String pPassword)
	{
		return ShadowVPNConfigureEditFragment.newInstance(pTitle, pServerIP, pPort, pPassword, ShadowVPNConfigureHelper.DEFAULT_LOCAL_IP, ShadowVPNConfigureHelper.DEFAULT_MAXIMUM_TRANSMISSION_UNITS);
	}

	public static ShadowVPNConfigureEditFragment newInstance(final String pTitle, final String pServerIP, final int pPort, final String pPassword, final String pLocalIP, final int pMaximumTransmissionUnits)
	{
		final ShadowVPNConfigureEditFragment fragment = new ShadowVPNConfigureEditFragment();

		final Bundle arguments = new Bundle();
		arguments.putString(ShadowVPNConfigureEditFragment.KEY_TITLE, pTitle);
		arguments.putString(ShadowVPNConfigureEditFragment.KEY_SERVER_IP, pServerIP);
		arguments.putInt(ShadowVPNConfigureEditFragment.KEY_PORT, pPort);
		arguments.putString(ShadowVPNConfigureEditFragment.KEY_PASSWORD, pPassword);
		arguments.putString(ShadowVPNConfigureEditFragment.KEY_LOCAL_IP, pLocalIP);
		arguments.putInt(ShadowVPNConfigureEditFragment.KEY_MAXIMUM_TRANSMISSION_UNITS, pMaximumTransmissionUnits);
		fragment.setArguments(arguments);

		return fragment;
	}

	private String mTitle;

	private String mServerIP;

	private int mPort;

	private String mPassword;

	private String mLocalIP;

	private int mMaximumTransmissionUnits;

	private FloatingLabelEditText mTitleText;

	private FloatingLabelEditText mServerIPText;

	private FloatingLabelEditText mPortText;

	private FloatingLabelEditText mPasswordText;

	private FloatingLabelEditText mLocalIPText;

	private FloatingLabelEditText mMaximumTransmissionUnitsText;

	@Override
	public void onCreate(final Bundle pSavedInstanceState)
	{
		super.onCreate(pSavedInstanceState);

		if (this.getArguments() != null)
		{
			this.mTitle = this.getArguments().getString(ShadowVPNConfigureEditFragment.KEY_TITLE);
			this.mServerIP = this.getArguments().getString(ShadowVPNConfigureEditFragment.KEY_SERVER_IP);
			this.mPort = this.getArguments().getInt(ShadowVPNConfigureEditFragment.KEY_PORT);
			this.mPassword = this.getArguments().getString(ShadowVPNConfigureEditFragment.KEY_PASSWORD);
			this.mLocalIP = this.getArguments().getString(ShadowVPNConfigureEditFragment.KEY_LOCAL_IP);
			this.mMaximumTransmissionUnits = this.getArguments().getInt(ShadowVPNConfigureEditFragment.KEY_MAXIMUM_TRANSMISSION_UNITS);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater pLayoutInflater, final ViewGroup pContainer, final Bundle pSavedInstanceState)
	{
		final View view = pLayoutInflater.inflate(R.layout.fragment_shadow_vpn_configure_edit, pContainer, false);

		this.mTitleText = (FloatingLabelEditText) view.findViewById(R.id.text_title);
		this.mTitleText.setInputWidgetText(this.mTitle);

		this.mServerIPText = (FloatingLabelEditText) view.findViewById(R.id.text_server_ip);
		this.mServerIPText.setInputWidgetText(this.mServerIP);

		this.mPortText = (FloatingLabelEditText) view.findViewById(R.id.text_port);
		this.mPortText.setInputWidgetText(String.valueOf(this.mPort));

		this.mPasswordText = (FloatingLabelEditText) view.findViewById(R.id.text_password);
		this.mPasswordText.setInputWidgetText(this.mPassword);

		this.mLocalIPText = (FloatingLabelEditText) view.findViewById(R.id.text_local_ip);
		this.mLocalIPText.setInputWidgetText(this.mLocalIP);

		this.mMaximumTransmissionUnitsText = (FloatingLabelEditText) view.findViewById(R.id.text_maximum_transmission_units);
		this.mMaximumTransmissionUnitsText.setInputWidgetText(String.valueOf(this.mMaximumTransmissionUnits));

		return view;
	}

	@Override
	public void onDestroyView()
	{
		final String title = this.mTitleText.getInputWidgetText().toString();
		final String serverIP = this.mServerIPText.getInputWidgetText().toString();
		final int port = Integer.parseInt(this.mPortText.getInputWidgetText().toString());
		final String password = this.mPasswordText.getInputWidgetText().toString();
		final String localIP = this.mLocalIPText.getInputWidgetText().toString();
		final int maximumTransmissionUnits = Integer.parseInt(this.mMaximumTransmissionUnitsText.getInputWidgetText().toString());

		if (!TextUtils.isEmpty(title))
		{
			if (TextUtils.isEmpty(this.mTitle))
			{
				ShadowVPNConfigureHelper.create(this.getActivity(), title, serverIP, port, password, localIP, maximumTransmissionUnits);
			}
			else
			{
				final ShadowVPNConfigure shadowVPNConfigure = ShadowVPNConfigureHelper.exists(this.getActivity(), this.mTitle);

				if (shadowVPNConfigure != null)
				{
					ShadowVPNConfigureHelper.update(this.getActivity(), shadowVPNConfigure, title, serverIP, port, password, localIP, maximumTransmissionUnits, shadowVPNConfigure.isSelected());
				}
				else
				{
					ShadowVPNConfigureHelper.create(this.getActivity(), title, serverIP, port, password, localIP, maximumTransmissionUnits);
				}
			}
		}

		super.onDestroyView();
	}
}