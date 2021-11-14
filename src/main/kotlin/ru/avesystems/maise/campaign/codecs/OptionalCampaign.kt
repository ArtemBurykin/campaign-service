package ru.avesystems.maise.campaign.codecs

import ru.avesystems.maise.campaign.domain.Campaign

/**
 * The class to hold data of a campaign or null through the bus.
 */
class OptionalCampaign(val campaign: Campaign?) {
}
