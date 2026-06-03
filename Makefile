.PHONY: dev-db backend frontend build

dev-db:
	docker compose up -d postgres

backend:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

frontend:
	cd frontend && npm run dev

build:
	cd backend && ./mvnw package && cd ../frontend && npm run build
