#!/bin/bash

usage() {
    echo "Usage: $0 [--cli] [--dry-run] --action <action> --env-file <env-file>"
    echo "Options:"
    echo "  --cli      Run in non-interactive mode"
    echo "  --dry-run  Print the command without executing"
    echo "  --action   Specify action [deploy_all, redeploy_bot, update]"
    echo "  --env-file Specify the .env file path"
    exit 1
}

if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml not found, you should call this script in the same directory as the docker-compose.yml"
    exit 1
fi

ACTION=""
ENV_FILE=""
CLI_MODE=0
DRY_RUN=0

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --cli) CLI_MODE=1 ;;
        --dry-run) DRY_RUN=1 ;;
        --action) ACTION="$2"; shift ;;
        --env-file) ENV_FILE="$2"; shift ;;
        --help) usage ;;
        *) usage ;;
    esac
    shift
done

execute_action() {
    local command=$1
    [[ $DRY_RUN -eq 1 ]] && echo "Dry run: $command" || eval $command
}

if [[ $CLI_MODE -eq 0 ]]; then
    select env_file in "production.env" ".env" "Exit"; do
        case $env_file in
            "production.env" | ".env") ENV_FILE=$env_file; break ;;
            "Exit") exit ;;
            *) echo "Invalid option." ;;
        esac
    done

    select action in "Deploy All" "Redeploy Just Bot" "Update" "Exit"; do
        case $action in
            "Deploy All") ACTION="docker compose --env-file $ENV_FILE up -d" ;;
            "Redeploy Just Bot") ACTION="docker compose --env-file $ENV_FILE up -d --no-deps --build family" ;;
            "Update") ACTION="docker compose pull && docker compose --env-file $ENV_FILE up -d" ;;
            "Exit") exit ;;
            *) echo "Invalid option." ;;
        esac
    done
else
    if [[ -z $ACTION || -z $ENV_FILE ]]; then
        echo "Error: Missing arguments."
        usage
    elif [ ! -f "$ENV_FILE" ]; then
        echo "Error: $ENV_FILE not found."
        usage
    else
        case $ACTION in
            "deploy_all") ACTION="docker compose --env-file $ENV_FILE up -d" ;;
            "redeploy_bot") ACTION="docker compose --env-file $ENV_FILE up -d --no-deps --build family" ;;
            "update") ACTION="docker compose pull && docker compose --env-file $ENV_FILE up -d" ;;
            *) echo "Invalid action: [$ACTION]."; usage ;;
        esac
    fi
fi

execute_action "$ACTION"
