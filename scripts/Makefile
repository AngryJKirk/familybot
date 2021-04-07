deploy-test:
	docker-compose up -d

deploy-prod:
	docker-compose --env-file /root/production.env up -d

redeploy:
	docker-compose --env-file /root/production.env up -d --no-deps --build family
