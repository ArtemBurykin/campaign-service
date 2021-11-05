package ru.avesystems.maise.campaign.models

import java.util.*

/**
 * The campaign object for a list of campaigns. Collections of these objects should be return from the read side.
 */
data class CampaignListItem(
    val id: UUID,
    val title: String
)
