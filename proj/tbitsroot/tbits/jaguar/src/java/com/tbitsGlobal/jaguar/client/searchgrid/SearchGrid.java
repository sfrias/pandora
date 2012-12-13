package com.tbitsGlobal.jaguar.client.searchgrid;

import java.util.List;

import com.tbitsGlobal.jaguar.client.events.OnRequestsRecieved;
import com.tbitsGlobal.jaguar.client.plugins.IGridContextMenuPlugin;
import com.tbitsGlobal.jaguar.client.plugins.slots.MOMPluginSlot;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.ITbitsEventHandle;
import commons.com.tbitsGlobal.utils.client.grids.GridColumnView;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.plugins.GWTPluginRegister;
import commons.com.tbitsGlobal.utils.client.search.grids.AbstractSearchGrid;

/**
 * 
 * @author sourabh
 * 
 * The Basic Search Grid
 */
public class SearchGrid extends AbstractSearchGrid{
	
	public SearchGrid(String sysPrefix) {
		super(sysPrefix);
		
		if(this.sysPrefix.equals(ClientUtils.getSysPrefix())){
			/**
			 * Clear the grid add models again when the search results arrive.
			 */
			final ITbitsEventHandle<OnRequestsRecieved> requestsReceivedHandle = new ITbitsEventHandle<OnRequestsRecieved>() {
				public void handleEvent(OnRequestsRecieved event) {
					if (event.isBasicSearch()) {
						clearStore(); // Clear the store before adding new
										// requests

						List<TbitsTreeRequestData> requestList = ClientUtils.listToRequestTree(event.getDqlResults().getRequests(),
								REQUEST, PARENT_REQUEST_ID);

						if (requestList.size() == 0) {
							TbitsInfo.info("No records fetched");
						} else {
							// Sort the requests
							// List<TbitsTreeRequestData> sortedRequests =
							// ClientUtils.sortRequests(requestList);
							setSortOrder(event.getSortOrderColumn(), event.getSortDirection());
							addModels(requestList);
						}
						setDql(event.getDql());
						Log.info("Finished request rendering.");
					}
				}
			};
			observable.subscribe(OnRequestsRecieved.class,
					requestsReceivedHandle);
		}
		
		List<IGridContextMenuPlugin> plugins = GWTPluginRegister.getInstance().getPlugins(MOMPluginSlot.class, IGridContextMenuPlugin.class);
		if(plugins != null && plugins.size() > 0){ //Check for presence of plugins
			for(IGridContextMenuPlugin plugin : plugins){
				if(plugin.shouldExecute(sysPrefix)){
					menu = plugin.getWidget(this);
					break;
				}
			}
		}
	}
	
	public GridColumnView getViewId() {
		return GridColumnView.SearchGrid;
	}

}