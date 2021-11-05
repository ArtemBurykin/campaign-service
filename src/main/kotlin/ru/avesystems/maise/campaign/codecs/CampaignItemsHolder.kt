package ru.avesystems.maise.campaign.codecs

import ru.avesystems.maise.campaign.models.CampaignListItem

class CampaignItemsHolder(var data: List<CampaignListItem>) {
    override fun toString(): String {
        return "Holder{$data}"
    }
}
