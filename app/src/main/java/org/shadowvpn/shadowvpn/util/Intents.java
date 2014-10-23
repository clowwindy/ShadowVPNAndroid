package org.shadowvpn.shadowvpn.util;

import android.content.Context;
import android.content.Intent;

import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;
import org.shadowvpn.shadowvpn.ui.activity.ShadowVPNConfigureEditActivity;

public class Intents
{
	public static void addShadowVPNConfigure(final Context pContext)
	{
		final Intent intent = new Intent(pContext, ShadowVPNConfigureEditActivity.class);
		pContext.startActivity(intent);
	}

	public static void editShadowVPNConfigure(final Context pContext, final ShadowVPNConfigure pShadowVPNConfigure)
	{
		final Intent intent = new Intent(pContext, ShadowVPNConfigureEditActivity.class);
		intent.putExtra(ShadowVPNConfigureEditActivity.EXTRA_TITLE, pShadowVPNConfigure.getTitle());

		pContext.startActivity(intent);
	}
}