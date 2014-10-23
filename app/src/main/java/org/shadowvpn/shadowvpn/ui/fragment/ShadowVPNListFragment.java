package org.shadowvpn.shadowvpn.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import org.shadowvpn.shadowvpn.R;
import org.shadowvpn.shadowvpn.model.ShadowVPNConfigure;
import org.shadowvpn.shadowvpn.ui.adapter.ShadowVPNConfigureAdapter;
import org.shadowvpn.shadowvpn.util.ShadowVPNConfigureHelper;

public class ShadowVPNListFragment extends ListFragment
{
	private static final int MENU_ID_STOP = 0x01;

	private static final int MENU_ID_EDIT = 0x02;

	private static final int MENU_ID_DELETE = 0x03;

	public static ShadowVPNListFragment newInstance()
	{
		final ShadowVPNListFragment fragment = new ShadowVPNListFragment();
		final Bundle arguments = new Bundle();
		fragment.setArguments(arguments);
		return fragment;
	}

	private ShadowVPNConfigureAdapter mShadowVPNConfigureAdapter;

	private IOnFragmentInteractionListener mListener;

	public ShadowVPNListFragment()
	{
	}

	@Override
	public void onAttach(final Activity pActivity)
	{
		super.onAttach(pActivity);

		try
		{
			this.mListener = (IOnFragmentInteractionListener) pActivity;
		}
		catch (final ClassCastException e)
		{
			throw new ClassCastException(pActivity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onViewCreated(final View pView, final Bundle pSavedInstanceState)
	{
		super.onViewCreated(pView, pSavedInstanceState);

		this.mShadowVPNConfigureAdapter = new ShadowVPNConfigureAdapter(this.getActivity(), ShadowVPNConfigureHelper.getAll(this.getActivity()));

		this.setListAdapter(this.mShadowVPNConfigureAdapter);

		this.registerForContextMenu(this.getListView());
	}

	@Override
	public void onDestroyView()
	{
		this.unregisterForContextMenu(this.getListView());

		super.onDestroyView();
	}

	@Override
	public void onDetach()
	{
		super.onDetach();

		this.mListener = null;
	}

	@Override
	public void onListItemClick(final ListView pListView, final View pView, final int pPosition, final long pId)
	{
		super.onListItemClick(pListView, pView, pPosition, pId);

		final ShadowVPNConfigure shadowVPNConfigure = this.mShadowVPNConfigureAdapter.getItem(pPosition);

		if (this.mListener != null)
		{
			this.mListener.onShadowVPNConfigureClick(shadowVPNConfigure);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu pContextMenu, final View pView, final ContextMenuInfo pContextMenuInfo)
	{
		super.onCreateContextMenu(pContextMenu, pView, pContextMenuInfo);

		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) pContextMenuInfo;

		final ShadowVPNConfigure configure = this.mShadowVPNConfigureAdapter.getItem(menuInfo.position);

		pContextMenu.setHeaderTitle(configure.getTitle());

		if (configure.isSelected())
		{
			pContextMenu.add(Menu.NONE, ShadowVPNListFragment.MENU_ID_STOP, ShadowVPNListFragment.MENU_ID_STOP, R.string.context_menu_stop_configure);
		}
		else
		{
			pContextMenu.add(Menu.NONE, ShadowVPNListFragment.MENU_ID_EDIT, ShadowVPNListFragment.MENU_ID_EDIT, R.string.context_menu_edit_configure);
			pContextMenu.add(Menu.NONE, ShadowVPNListFragment.MENU_ID_DELETE, ShadowVPNListFragment.MENU_ID_DELETE, R.string.context_menu_delete_configure);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem pMenuItem)
	{
		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) pMenuItem.getMenuInfo();

		final ShadowVPNConfigure configure = this.mShadowVPNConfigureAdapter.getItem(menuInfo.position);

		switch (pMenuItem.getItemId())
		{
			case ShadowVPNListFragment.MENU_ID_STOP:
				if (this.mListener != null)
				{
					this.mListener.onShadowVPNConfigureStop(configure);
				}
				return true;
			case ShadowVPNListFragment.MENU_ID_EDIT:
				if (this.mListener != null)
				{
					this.mListener.onShadowVPNConfigureEdit(configure);
				}
				return true;
			case ShadowVPNListFragment.MENU_ID_DELETE:
				if (this.mListener != null)
				{
					this.mListener.onShadowVPNConfigureDelete(configure);
				}
				return true;
			default:
				return super.onContextItemSelected(pMenuItem);
		}
	}

	public interface IOnFragmentInteractionListener
	{
		public void onShadowVPNConfigureClick(final ShadowVPNConfigure pShadowVPNConfigure);

		public void onShadowVPNConfigureStop(final ShadowVPNConfigure pShadowVPNConfigure);

		public void onShadowVPNConfigureEdit(final ShadowVPNConfigure pShadowVPNConfigure);

		public void onShadowVPNConfigureDelete(final ShadowVPNConfigure pShadowVPNConfigure);
	}
}