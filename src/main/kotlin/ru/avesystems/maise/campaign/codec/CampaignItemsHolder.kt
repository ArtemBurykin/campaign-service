package ru.avesystems.maise.campaign.codec

import ru.avesystems.maise.campaign.model.CampaignListItem

class CampaignItemsHolder(var data: List<CampaignListItem>) {
    override fun toString(): String {
        return "Holder{$data}"
    }
}
