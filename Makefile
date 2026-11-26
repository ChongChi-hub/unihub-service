.PHONY: up-infra up down logs ps reset-db build check format

COMPOSE := docker compose

up-infra:
	$(COMPOSE) --profile infra up -d

up:
	$(COMPOSE) --profile core up -d

down:
	$(COMPOSE) --profile full down

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

reset-db:
	$(COMPOSE) --profile full down -v
	$(COMPOSE) --profile infra up -d

build:
	./gradlew build

check:
	./gradlew spotlessCheck check

format:
	./gradlew spotlessApply
