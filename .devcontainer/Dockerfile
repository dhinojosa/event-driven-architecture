FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install prerequisites
RUN apt-get update && \
    apt-get install -y curl zip unzip && \
    rm -rf /var/lib/apt/lists/*

# Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash

# Ensure sdkman is available for all future shells
ENV SDKMAN_DIR="/home/vscode/.sdkman"
ENV PATH="${SDKMAN_DIR}/bin:${SDKMAN_DIR}/candidates/java/current/bin:${PATH}"

# Activate SDKMAN for default user (vscode)
RUN echo "source ${SDKMAN_DIR}/bin/sdkman-init.sh" >> /home/vscode/.bashrc && \
    chown -R vscode:vscode /home/vscode/.sdkman

USER vscode
WORKDIR /workspaces