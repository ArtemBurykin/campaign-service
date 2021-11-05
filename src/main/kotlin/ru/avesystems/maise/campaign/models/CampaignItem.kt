package ru.avesystems.maise.campaign.models

import ru.avesystems.maise.campaign.domain.CampaignState
import java.util.*

/**
 * Describes the item of a campaign returned from the read side of the service.
 */
data class CampaignItem(
    val id: UUID,
    val title: String,
    val templateId: UUID,
    val templateConfig: Map<String, Any>,
    val state: CampaignState,
    val recipients: Map<UUID, Map<String, Any>>
)
