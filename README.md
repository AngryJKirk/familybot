# familybot

Telegram Bot for my friends

![](readme_assets/j.png)
Wake up Neo

You obosralsya

# Documentation

There are [docs](./DOCUMENTATION.md) now, check it out! They are not yet finished, but I am trying. Feel free to fix my
grammar or anything you would like to.

# Stats

Total amount of chats and users that have been seen by the bot:
![](readme_assets/stats1.png)

Amount of everyday messages passed through the bot:
![](readme_assets/stats2.png)

# Build & deploy

### Requirments

1. [just](https://github.com/casey/just)
2. docker
3. docker compose

### Deploy test

1. Fill environment variables in `scripts/.env`.

2. run `just deploy`

To re-deploy only the app run `just redeploy`.

### Deploy production

1. Create `production.env` in `/scripts` folder just like `scripts/.env` but with production params

2. run `just env=production deploy`

To re-deploy only the app run `just env=production redeploy`.

### Troubleshooting:

1. Old versions of docker-compose do not support `--env-file` which is required for production.

2. If you have issues installing `just`, just RTFM 🕊️ or copy the commands from [justfile](justfile)`

# Disclaimer

This project may seem offensive. You have been warned. It was made just for fun, I don't have any negative feelings
towards anyone.

# Thanks

This project is written using JetBrains All Products Pack, provided
by [JetBrains](https://www.jetbrains.com/?from=familybot).
