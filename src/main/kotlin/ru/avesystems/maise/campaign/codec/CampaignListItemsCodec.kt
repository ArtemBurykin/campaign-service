package ru.avesystems.maise.campaign.codec

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

/**
 * The codec to transfer collections of campaign list items through the event bus locally.
 */
class CampaignListItemsCodec : MessageCodec<CampaignItemsHolder, CampaignItemsHolder> {

    override fun transform(holder: CampaignItemsHolder): CampaignItemsHolder {
        return holder
    }

    override fun name(): String {
        return "CampaignListItemsCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer?, holder: CampaignItemsHolder?) {}
    override fun decodeFromWire(pos: Int, buffer: Buffer?): CampaignItemsHolder? {
        return null
    }
}