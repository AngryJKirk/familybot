deploy-test:
	docker-compose up -d -e BOT_NAME  -e BOT_TOKEN -e BOT_OWNER_NAME

deploy-prod:
	docker-compose up -d --env-file=~/production.env

redeploy:
	docker-compose up -d --no-deps --build family
