.PHONY: build clean test verify sign publish install run-ide help

PLUGIN_NAME := pi-jetbrains-plugin
VERSION := $(shell grep '^version' build.gradle.kts | head -1 | sed 's/.*"\(.*\)"/\1/')
JAR := build/libs/$(PLUGIN_NAME)-$(VERSION).jar
ZIP := build/distributions/$(PLUGIN_NAME)-$(VERSION).zip

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Build plugin jar
	./gradlew buildPlugin

clean: ## Clean build artifacts
	./gradlew clean

test: ## Run tests
	./gradlew test

verify: ## Run plugin verifier (compatibility check)
	./gradlew verifyPlugin

sign: ## Sign plugin (requires CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD env vars)
	./gradlew signPlugin

publish: ## Publish to JetBrains Marketplace (requires PUBLISH_TOKEN env var)
	./gradlew publishPlugin

install: build ## Build and show install path
	@echo ""
	@echo "Plugin zip: build/distributions/$(PLUGIN_NAME)-$(VERSION).zip"
	@echo "Plugin jar: $(JAR)"
	@echo ""
	@echo "Install: IDE → Settings → Plugins → ⚙️ → Install Plugin from Disk → select zip or jar"
	@echo "Upload: https://plugins.jetbrains.com → Upload Update → select zip"

run-ide: ## Launch a sandbox IDE with the plugin loaded
	./gradlew runIde
