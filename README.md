# familybot

Bot for my friends

![](https://storozhenko.dev/images/jesus_final-min.jpg)

Wake up Neo

You obosralsya

# Disclaimer

This project may seem offensive. You have been warned. It was made just for fun, I don't have any negative feelings
towards anyone.

# Documentation

There are [docs](./DOCUMENTATION.md) now, check it out! They are not yet finished, but I am trying. Feel free to fix my
grammar or anything you would like to.

# Stats

Total amount of chats and users that have been seen by the bot:
![](https://storozhenko.dev/stats1.png)

Amount of everyday messages passed through the bot:
![](https://storozhenko.dev/stats2.png)

# Build & deploy

### Requirments

1. make
2. docker
3. docker-compose

### Deploy test

1. Fill environment variables in `scripts/.env`.

2. enter `scripts/` and run `make deploy-test`

To re-deploy only the app run `make redeploy-test`.

### Deploy prod

1. Create production.env in your home folder just like `scripts/.env` but with production params

2. enter `scripts/` and run `make deploy-prod`

To re-deploy only the app run `make redeploy-prod`.

### Troubleshooting:

1. Old versions of docker-compose do not support `--env-file` which is required for production.

3. If you have issues installing `make`, just copy commands from `scripts/Makefile`

# Thanks

This project is written using JetBrains All Products Pack, provided
by [JetBrains](https://www.jetbrains.com/?from=familybot).

Thanks [z3k1k](https://github.com/z3d1k) for cool ktlint Maven plugin.
