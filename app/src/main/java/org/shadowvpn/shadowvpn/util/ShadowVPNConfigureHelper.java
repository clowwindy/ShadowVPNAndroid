package org.shadowvpn.shadowvpn.util;

import android.content.Context;

import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ShadowVPNConfigureHelper
{
	public static final String DEFAULT_LOCAL_IP = "10.7.0.2";

	public static final int DEFAULT_MAXIMUM_TRANSMISSION_UNITS = 1440;

	public static final int DEFAULT_CONCURRENCY = 1;

	public static ShadowVPNConfigure create(final Context pContext, final String pTitle, final String pServerIP, final int pPort, final String pPassword, final String pLocalIP, final int pMaximumTransmissionUnits, final int pConcurrency, final boolean pBypassChinaRoutes)
	{
		final Realm realm = Realm.getInstance(pContext);
		realm.beginTransaction();

		final ShadowVPNConfigure configure = realm.createObject(ShadowVPNConfigure.class);
		configure.setTitle(pTitle);
		configure.setServerIP(pServerIP);
		configure.setPort(pPort);
		configure.setPassword(pPassword);
		configure.setLocalIP(pLocalIP);
		configure.setMaximumTransmissionUnits(pMaximumTransmissionUnits);
		configure.setConcurrency(pConcurrency);
		configure.setBypassChinaRoutes(pBypassChinaRoutes);

		realm.commitTransaction();

		return configure;
	}

	public static void delete(final Context pContext, final String pTitle)
	{
		final Realm realm = Realm.getInstance(pContext);
		realm.beginTransaction();

		final RealmQuery<ShadowVPNConfigure> shadowVPNConfigureRealmQuery = realm.where(ShadowVPNConfigure.class);
		shadowVPNConfigureRealmQuery.equalTo("title", pTitle);

		shadowVPNConfigureRealmQuery.findAll().clear();

		realm.commitTransaction();
	}

	public static ShadowVPNConfigure exists(final Context pContext, final String pTitle)
	{
		final Realm realm = Realm.getInstance(pContext);

		final RealmQuery<ShadowVPNConfigure> shadowVPNConfigureRealmQuery = realm.where(ShadowVPNConfigure.class);
		shadowVPNConfigureRealmQuery.equalTo("title", pTitle);

		return shadowVPNConfigureRealmQuery.findFirst();
	}

	public static RealmResults<ShadowVPNConfigure> getAll(final Context pContext)
	{
		final Realm realm = Realm.getInstance(pContext);
		realm.beginTransaction();

		final RealmQuery<ShadowVPNConfigure> query = realm.where(ShadowVPNConfigure.class);
		final RealmResults<ShadowVPNConfigure> configures = query.findAll();

		realm.commitTransaction();

		return configures;
	}

	public static ShadowVPNConfigure update(final Context pContext, final ShadowVPNConfigure pShadowVPNConfigure, final String pTitle, final String pServerIP, final int pPort, final String pPassword, final String pLocalIP, final int pMaximumTransmissionUnits, final int pConcurrency, final boolean pBypassChinaRoutes, final boolean pSelected)
	{
		final Realm realm = Realm.getInstance(pContext);
		realm.beginTransaction();

		pShadowVPNConfigure.setTitle(pTitle);
		pShadowVPNConfigure.setServerIP(pServerIP);
		pShadowVPNConfigure.setPort(pPort);
		pShadowVPNConfigure.setPassword(pPassword);
		pShadowVPNConfigure.setLocalIP(pLocalIP);
		pShadowVPNConfigure.setMaximumTransmissionUnits(pMaximumTransmissionUnits);
		pShadowVPNConfigure.setConcurrency(pConcurrency);
		pShadowVPNConfigure.setBypassChinaRoutes(pBypassChinaRoutes);
		pShadowVPNConfigure.setSelected(pSelected);

		realm.commitTransaction();

		return pShadowVPNConfigure;
	}

	public static void selectShadowVPNConfigure(final Context pContext, final String pTitle)
	{
		final ShadowVPNConfigure configure = ShadowVPNConfigureHelper.exists(pContext, pTitle);

		if (configure != null)
		{
			ShadowVPNConfigureHelper.update(pContext, configure, configure.getTitle(), configure.getServerIP(), configure.getPort(), configure.getPassword(), configure.getLocalIP(), configure.getMaximumTransmissionUnits(), configure.getConcurrency(), configure.isBypassChinaRoutes(), true);
		}
	}

	public static void resetAllSelectedValue(final Context pContext)
	{
		final RealmResults<ShadowVPNConfigure> shadowVPNConfigureRealmResults = ShadowVPNConfigureHelper.getAll(pContext);

		final Realm realm = Realm.getInstance(pContext);
		realm.beginTransaction();

		for (final ShadowVPNConfigure configure : shadowVPNConfigureRealmResults)
		{
			configure.setSelected(false);
		}

		realm.commitTransaction();
	}
}