package ru.avesystems.maise.campaign.codecs

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

/**
 * The codec to pass data of a campaign or null through the inner bus of Vertx
 */
class OptionalCampaignCodec : MessageCodec<OptionalCampaign, OptionalCampaign> {
    override fun transform(campaign: OptionalCampaign): OptionalCampaign {
        return campaign
    }

    override fun name(): String {
        return "OptionalCampaignCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer?, holder: OptionalCampaign?) {}
    override fun decodeFromWire(pos: Int, buffer: Buffer?): OptionalCampaign? {
        return null
    }
}