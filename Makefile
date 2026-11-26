.PHONY: up-infra up down logs logs-all ps reset-db build check format start-all stop-all status-all

COMPOSE := docker compose

up-infra:
	$(COMPOSE) --profile infra up -d

up:
	$(COMPOSE) --profile core up -d

down:
	$(COMPOSE) --profile full down

logs:
	$(COMPOSE) logs -f

logs-all:
	tail -n 50 -f logs/*.log

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

start-all:
	./deploy/start-all.sh

stop-all:
	./deploy/stop-all.sh

status-all:
	@docker compose ps
	@echo "--- Trạng thái Java Services ---"
	@for name in identity-service academic-service api-gateway; do \
		f=".pids/$$name.pid"; \
		if [ -f "$$f" ] && kill -0 $$(cat "$$f") 2>/dev/null; then \
			echo "$$name: ĐANG CHẠY (PID $$(cat "$$f"))"; \
		else \
			echo "$$name: ĐÃ TẮT"; \
		fi; \
	done
