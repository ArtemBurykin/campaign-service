package ru.avesystems.maise.campaign.domain

class UnknownEventTypeException(message: String = "Unknown event type"): Exception(message)

class CampaignNotStartedException(message: String = "The campaign is not started"): Exception(message)

class CampaignAlreadyStartedException(message: String = "The campaign is already started"): Exception(message)

class CampaignCannotBeResumedException(message: String = "The campaign cannot be resumed"): Exception(message)

class CampaignAlreadyPausedException(message: String = "The campaign already paused"): Exception(message)

class AlreadyStartedCampaignCannotBeDeletedException(
    message: String = "You cannot delete a started campaign"
): Exception(message)

