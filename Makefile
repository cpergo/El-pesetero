SHELL := /bin/sh
.DEFAULT_GOAL := help

PROJECT_ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
XCODE_PROJECT := $(PROJECT_ROOT)/iosApp/iosApp.xcodeproj
XCODE_SCHEME := iosApp
IOS_CONFIGURATION ?= Debug
IOS_DERIVED_DATA ?= $(PROJECT_ROOT)/build/ios-derived-data
IOS_APP_NAME := iosApp.app

SIMULATOR ?= iPhone 17 Pro
SIMULATOR_ID ?=
IOS_DEVICE ?=
IOS_DEVICE_ID ?=
APPLE_TEAM_ID ?=
IOS_BUNDLE_ID ?= com.pesetas.ios

ANDROID_STUDIO_JBR := /Applications/Android Studio.app/Contents/jbr/Contents/Home
IOS_JAVA_HOME ?= $(shell \
	if [ -x "$(ANDROID_STUDIO_JBR)/bin/java" ]; then \
		printf '%s' "$(ANDROID_STUDIO_JBR)"; \
	elif [ -x "$$JAVA_HOME/bin/java" ]; then \
		printf '%s' "$$JAVA_HOME"; \
	else \
		/usr/libexec/java_home -v 21 2>/dev/null || \
		/usr/libexec/java_home -v 17 2>/dev/null || true; \
	fi)

-include $(PROJECT_ROOT)/Makefile.local

.PHONY: help check-ios check-ios-simulator ios ios-open ios-sim ios-simulators ios-devices ios-device ios-test

help:
	@printf '%s\n' \
		'El pesetero — comandos iOS' \
		'' \
		'  make ios                  Compila, instala y abre la app en el simulador' \
		'  make ios-open             Abre el proyecto en Xcode' \
		'  make ios-simulators       Lista los simuladores disponibles' \
		'  make ios-devices          Lista los iPhone físicos disponibles' \
		'  make ios-device           Compila, instala y abre la app en un iPhone' \
		'  make ios-test             Ejecuta las pruebas Kotlin para iOS' \
		'' \
		'Variables habituales:' \
		'  SIMULATOR="iPhone 17 Pro"' \
		'  SIMULATOR_ID=<UUID>' \
		'  IOS_DEVICE="Nombre del iPhone" o IOS_DEVICE_ID=<UDID>' \
		'  APPLE_TEAM_ID=<Team ID>' \
		'  IOS_BUNDLE_ID=com.ejemplo.elpesetero'

check-ios:
	@if [ "$$(uname -s)" != 'Darwin' ]; then \
		printf '%s\n' 'Error: los comandos iOS requieren macOS.' >&2; \
		exit 2; \
	fi
	@if ! command -v xcodebuild >/dev/null 2>&1 || ! command -v xcrun >/dev/null 2>&1; then \
		printf '%s\n' 'Error: instala Xcode y selecciona sus Command Line Tools.' >&2; \
		exit 2; \
	fi
	@if [ ! -d "$(XCODE_PROJECT)" ]; then \
		printf 'Error: no se encuentra %s\n' "$(XCODE_PROJECT)" >&2; \
		exit 2; \
	fi
	@if [ -z "$(IOS_JAVA_HOME)" ] || [ ! -x "$(IOS_JAVA_HOME)/bin/java" ]; then \
		printf '%s\n' 'Error: se necesita JDK 17 o 21. Instala Android Studio o define IOS_JAVA_HOME.' >&2; \
		exit 2; \
	fi
	@java_major="$$('$(IOS_JAVA_HOME)/bin/java' -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -n 1)"; \
	case "$$java_major" in \
		17|21) ;; \
		*) printf 'Error: IOS_JAVA_HOME debe apuntar a JDK 17 o 21; se detectó Java %s.\n' "$$java_major" >&2; exit 2 ;; \
	esac

check-ios-simulator: check-ios
	@if [ "$$(uname -m)" != 'arm64' ]; then \
		printf '%s\n' 'Error: el simulador configurado requiere un Mac con Apple Silicon (arm64).' >&2; \
		printf '%s\n' 'En un Mac Intel puedes usar un iPhone físico o añadir el target iosX64.' >&2; \
		exit 2; \
	fi

ios: ios-sim

ios-open: check-ios
	@open "$(XCODE_PROJECT)"

ios-simulators: check-ios
	@xcrun simctl list devices available

ios-devices: check-ios
	@devices="$$(xcodebuild -project "$(XCODE_PROJECT)" -scheme "$(XCODE_SCHEME)" -showdestinations 2>/dev/null | awk '/platform:iOS, arch:/')"; \
	if [ -n "$$devices" ]; then \
		printf '%s\n' "$$devices"; \
	else \
		printf '%s\n' 'No hay ningún iPhone disponible. Conéctalo, desbloquéalo y acepta «Confiar».'; \
	fi

ios-test: check-ios-simulator
	@cd "$(PROJECT_ROOT)" && \
		JAVA_HOME="$(IOS_JAVA_HOME)" ./gradlew :shared:iosSimulatorArm64Test

ios-sim: check-ios-simulator
	@set -eu; \
	simulator_id="$(SIMULATOR_ID)"; \
	if [ -z "$$simulator_id" ]; then \
		simulator_id="$$(xcrun simctl list devices available | \
			sed -nE 's/^[[:space:]]*(.*) \(([0-9A-Fa-f-]{36})\) \((Booted|Shutdown)\).*$$/\1|\2|\3/p' | \
			awk -F '|' -v wanted="$(SIMULATOR)" ' \
			{ \
				if ($$1 == wanted) { \
					last=$$2; \
					if ($$3 == "Booted") { print $$2; booted=1; exit } \
				} \
			} \
			END { if (!booted && last != "") print last }')"; \
	fi; \
	if [ -z "$$simulator_id" ]; then \
		printf 'Error: no se encontró el simulador «%s». Ejecuta make ios-simulators.\n' "$(SIMULATOR)" >&2; \
		exit 2; \
	fi; \
	printf 'Preparando simulador %s (%s)…\n' "$(SIMULATOR)" "$$simulator_id"; \
	xcrun simctl bootstatus "$$simulator_id" -b; \
	open -a Simulator; \
	cd "$(PROJECT_ROOT)"; \
	JAVA_HOME="$(IOS_JAVA_HOME)" xcodebuild \
		-project "$(XCODE_PROJECT)" \
		-scheme "$(XCODE_SCHEME)" \
		-configuration "$(IOS_CONFIGURATION)" \
		-destination "platform=iOS Simulator,id=$$simulator_id" \
		-derivedDataPath "$(IOS_DERIVED_DATA)" \
		PRODUCT_BUNDLE_IDENTIFIER="$(IOS_BUNDLE_ID)" \
		build; \
	app_path="$(IOS_DERIVED_DATA)/Build/Products/$(IOS_CONFIGURATION)-iphonesimulator/$(IOS_APP_NAME)"; \
	if [ ! -d "$$app_path" ]; then \
		printf 'Error: Xcode no generó %s\n' "$$app_path" >&2; \
		exit 2; \
	fi; \
	bundle_id="$$(/usr/bin/plutil -extract CFBundleIdentifier raw "$$app_path/Info.plist")"; \
	xcrun simctl install "$$simulator_id" "$$app_path"; \
	xcrun simctl launch --terminate-running-process "$$simulator_id" "$$bundle_id"

ios-device: check-ios
	@set -eu; \
	device_ref="$(IOS_DEVICE_ID)"; \
	destination=''; \
	if [ -n "$$device_ref" ]; then \
		destination="platform=iOS,id=$$device_ref"; \
	elif [ -n "$(IOS_DEVICE)" ]; then \
		device_ref="$(IOS_DEVICE)"; \
		destination="platform=iOS,name=$(IOS_DEVICE)"; \
	else \
		printf '%s\n' 'Error: define IOS_DEVICE o IOS_DEVICE_ID. Usa make ios-devices para consultar los destinos.' >&2; \
		exit 2; \
	fi; \
	if [ -z "$(APPLE_TEAM_ID)" ]; then \
		printf '%s\n' 'Error: define APPLE_TEAM_ID con el equipo seleccionado en Xcode.' >&2; \
		printf '%s\n' 'Puedes guardarlo junto al dispositivo y bundle ID en Makefile.local.' >&2; \
		exit 2; \
	fi; \
	if [ "$(IOS_BUNDLE_ID)" = 'com.pesetas.ios' ]; then \
		printf '%s\n' 'Aviso: com.pesetas.ios puede no estar disponible para tu equipo; usa un identificador único si Xcode lo rechaza.'; \
	fi; \
	printf 'Compilando para %s con firma automática…\n' "$$device_ref"; \
	cd "$(PROJECT_ROOT)"; \
	JAVA_HOME="$(IOS_JAVA_HOME)" xcodebuild \
		-project "$(XCODE_PROJECT)" \
		-scheme "$(XCODE_SCHEME)" \
		-configuration "$(IOS_CONFIGURATION)" \
		-destination "$$destination" \
		-derivedDataPath "$(IOS_DERIVED_DATA)" \
		-allowProvisioningUpdates \
		-allowProvisioningDeviceRegistration \
		CODE_SIGN_STYLE=Automatic \
		DEVELOPMENT_TEAM="$(APPLE_TEAM_ID)" \
		PRODUCT_BUNDLE_IDENTIFIER="$(IOS_BUNDLE_ID)" \
		build; \
	app_path="$(IOS_DERIVED_DATA)/Build/Products/$(IOS_CONFIGURATION)-iphoneos/$(IOS_APP_NAME)"; \
	if [ ! -d "$$app_path" ]; then \
		printf 'Error: Xcode no generó %s\n' "$$app_path" >&2; \
		exit 2; \
	fi; \
	bundle_id="$$(/usr/bin/plutil -extract CFBundleIdentifier raw "$$app_path/Info.plist")"; \
	printf 'Instalando y abriendo %s…\n' "$$bundle_id"; \
	xcrun devicectl device install app --device "$$device_ref" "$$app_path"; \
	xcrun devicectl device process launch --device "$$device_ref" --terminate-existing "$$bundle_id"
