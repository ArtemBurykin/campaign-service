package ru.avesystems.maise.campaign.domain

/**
 * The exception should be thrown when we perform an action on a deleted campaign.
 */
class CampaignDeletedException(message: String = "The campaign is deleted"): Exception(message)

class UnknownEventTypeException(message: String = "Unknown event type"): Exception(message)
