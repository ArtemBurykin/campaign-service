package ru.avesystems.maise.campaign.domain

class UnknownEventTypeException(message: String = "Unknown event type"): Exception(message)

class CampaignNotStartedException(message: String = "The campaign is not started"): Exception(message)