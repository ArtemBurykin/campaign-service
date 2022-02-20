.DEFAULT_GOAL := help
help:
	@grep -E '^[a-zA-Z-]+:.*?## .*$$' Makefile | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "%s - %s\n", $$1, $$2}'
.PHONY: help

up-dev: ## Run the app in the dev env
	@docker-compose -f docker-compose.dev.yml up --build

up-test: ## Run the app for testing
	@docker-compose -f docker-compose.test.yml down --volumes && docker-compose -f docker-compose.test.yml up --build
