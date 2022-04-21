deploy-test:
	docker-compose up -d

redeploy-test:
	docker-compose up -d --no-deps --build family

deploy-prod:
	docker-compose --env-file ./production.env up -d

redeploy-prod:
	docker-compose --env-file ./production.env up -d --no-deps --build family

update:
	docker-compose pull && docker-compose --env-file ./production.env up -d

