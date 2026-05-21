package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System

object AppDataHolder {
    var selectedApp: Apps.AppQueryResponse? = null
    var selectedMarketplaceApp: Apps.AppAvailableItem ?= null
    var disks: List<System.DiskDetails> = emptyList()
}