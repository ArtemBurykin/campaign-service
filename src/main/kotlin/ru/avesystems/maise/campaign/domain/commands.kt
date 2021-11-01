package ru.avesystems.maise.campaign.domain

import java.util.*

data class CreateCampaign(
        val title: String,
        val templateTypeId: UUID,
        val templateTypeConfig: Map<String, Any>,
        val recipientLists: Map<UUID, Map<String, Any>>
)
