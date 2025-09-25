FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install tools needed for SDKMAN + Java
RUN apt-get update && \
    apt-get install -y curl zip unzip && \
    rm -rf /var/lib/apt/lists/*

# Switch to the default devcontainer user
USER vscode
WORKDIR /workspaces