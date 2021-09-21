package ru.avesystems.maise.campaign.domain

import java.util.*

data class CreateCampaign(
        val title: String,
        val templateTypeId: UUID,
        val templateTypeConfig: Config,
        val recipientLists: Map<UUID, Config>
)
