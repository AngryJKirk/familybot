set positional-arguments
env := "test"

env_file_path := if env == "test" {
  ""
} else if env == "production" {
  "--env-file ./production.env"
} else {
    error("The env_type should be one of those values: [test, production]. It is test by default, the passed value is " + env)
}

_cd:
    cd scripts

deploy: _cd
    docker compose up {{env_file_path}} -d

redeploy: _cd
    docker compose up {{env_file_path}} -d --no-deps  --build family

update: _cd
    docker compose pull && docker compose {{env_file_path}} -d

