package org.shadowvpn.shadowvpn.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ShadowVPNConfigureAdapter extends BaseAdapter implements RealmChangeListener
{
	private final Context mContext;

	private final RealmResults<ShadowVPNConfigure> mShadowVPNConfigures;

	public ShadowVPNConfigureAdapter(final Context pContext, @NonNull final RealmResults<ShadowVPNConfigure> pShadowVPNConfigureRealmResults)
	{
		this.mContext = pContext;

		this.mShadowVPNConfigures = pShadowVPNConfigureRealmResults;

		Realm.getInstance(this.mContext).addChangeListener(this);
	}

	public Context getContext()
	{
		return this.mContext;
	}

	@Override
	public int getCount()
	{
		return this.mShadowVPNConfigures.size();
	}

	@Override
	public ShadowVPNConfigure getItem(final int pPosition)
	{
		return this.mShadowVPNConfigures.get(pPosition);
	}

	@Override
	public long getItemId(final int pPosition)
	{
		return pPosition;
	}

	@Override
	public void onChange()
	{
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(final int pPosition, final View pConvertView, final ViewGroup pParent)
	{
		final View layout;

		if (pConvertView == null)
		{
			layout = LayoutInflater.from(this.mContext).inflate(R.layout.list_item_shadow_vpn_configure, pParent, false);

			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) layout.findViewById(R.id.icon);
			viewHolder.title = (TextView) layout.findViewById(R.id.title);
			viewHolder.summary = (TextView) layout.findViewById(R.id.summary);

			layout.setTag(viewHolder);
		}
		else
		{
			layout = pConvertView;
		}

		final ViewHolder viewHolder = (ViewHolder) layout.getTag();
		final ShadowVPNConfigure configure = this.getItem(pPosition);

		viewHolder.icon.setImageResource(configure.isSelected() ? R.drawable.ic_vpn_connected : R.drawable.ic_vpn_unconnected);
		viewHolder.title.setText(configure.getTitle());
		viewHolder.summary.setText(configure.getServerIP());

		return layout;
	}

	private static class ViewHolder
	{
		public ImageView icon;

		public TextView title;

		public TextView summary;
	}
}
