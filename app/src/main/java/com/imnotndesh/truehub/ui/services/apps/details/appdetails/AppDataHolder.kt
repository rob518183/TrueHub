package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import com.imnotndesh.truehub.data.models.Apps

object AppDataHolder {
    var selectedApp: Apps.AppQueryResponse? = null
    var selectedMarketplaceApp: Apps.AppAvailableItem ?= null
}