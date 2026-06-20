.PHONY: dev-db backend frontend build prod-build prod-up prod-down prod-logs

dev-db:
	docker compose up -d postgres

backend:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

frontend:
	cd frontend && npm run dev

build:
	cd backend && ./mvnw package && cd ../frontend && npm run build

prod-build:
	cd backend && ./mvnw package -DskipTests -B
	docker compose -f docker-compose.prod.yml build

prod-up:
	docker compose -f docker-compose.prod.yml up -d

prod-down:
	docker compose -f docker-compose.prod.yml down

prod-logs:
	docker compose -f docker-compose.prod.yml logs -f
