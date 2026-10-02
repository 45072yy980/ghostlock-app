API ?= 35

# Auto-detect NDK
ifeq ($(OS),Windows_NT)
  NDK_ROOT ?= $(subst \,/,$(firstword $(wildcard     $(subst \,/,$(LOCALAPPDATA))/Android/Sdk/ndk/*     $(subst \,/,$(ANDROID_HOME))/ndk/*     D:/AndroidSDK/ndk/*)))
  override NDK_ROOT := $(subst \,/,$(NDK_ROOT))
  PREBUILT := windows-x86_64
  CLANG_BASE := aarch64-linux-android$(API)-clang
  NDK_CC := $(NDK_ROOT)/toolchains/llvm/prebuilt/$(PREBUILT)/bin/$(CLANG_BASE).cmd
else
  NDK_ROOT ?= $(or $(ANDROID_NDK_HOME),$(ANDROID_NDK_ROOT))
  PREBUILT := linux-x86_64
  NDK_CC := $(NDK_ROOT)/toolchains/llvm/prebuilt/$(PREBUILT)/bin/aarch64-linux-android$(API)-clang
endif

SRCS := \
  src/core/main.c \
  src/core/offsets_json.c \
  src/core/util.c \
  src/core/fops.c

# Alternate exploit engine: CVE-2026-43074 (eventpoll UAF, self-contained).
# Built as its own standalone binary so it never clashes with the GhostLock
# symbol set (both use a "kernelsnitch" helper).
ALT43074_SRCS := \
  src/exploits/cve202643074/engine.c \
  src/exploits/cve202643074/util.c \
  src/exploits/cve202643074/patch.c \
  src/exploits/cve202643074/page.c \
  src/exploits/cve202643074/pipe.c \
  src/exploits/cve202643074/late_refs.c \
  src/exploits/cve202643074/su.c \
  src/exploits/cve202643074/payloads.S
ALT43074_HDRS := $(wildcard src/exploits/cve202643074/*.h src/exploits/cve202643074/*/*.h)

# Headers also trigger a rebuild (e.g. a freshly --register-ed src/kernels/<release>/offsets.h).
HDRS := $(wildcard src/core/*.h src/core/*/*.h src/kernels/*.h src/kernels/*/*.h)

# Device offsets are selected at runtime from uname -r.
TARGET_CONFIG ?= target.h

CFLAGS = -O2 -flto -Wall -Wno-unused-parameter -Wno-sign-compare -Wno-unused-function \
  -Isrc/core -Isrc/kernels -DTARGET_CONFIG_H=\"$(TARGET_CONFIG)\"
LDFLAGS := -fPIE -pie -pthread -flto

# CVE-2026-43074 engine: static, self-contained (it carries its own su daemon
# assembly payload and expects a raw aarch64 Android target).
ALT43074_CFLAGS = -O2 -g0 -Wall -Wextra -D_GNU_SOURCE -D__ARM \
  -ffunction-sections -fdata-sections -Isrc/exploits/cve202643074
ALT43074_LDFLAGS := -static -pthread -Wl,--gc-sections

.PHONY: all clean product

all: ghostlock ghostlock_43074

ghostlock: $(SRCS) $(HDRS)
	@echo "Using NDK compiler: $(NDK_CC)"
	@echo "Target config: $(TARGET_CONFIG)"
	$(NDK_CC) $(CFLAGS) $(LDFLAGS) $(filter %.c,$^) -o ghostlock

ghostlock_43074: $(ALT43074_SRCS) $(ALT43074_HDRS)
	@echo "Building CVE-2026-43074 engine"
	$(NDK_CC) $(ALT43074_CFLAGS) $(filter %.c %.S,$^) -o ghostlock_43074 $(ALT43074_LDFLAGS)

product: ghostlock ghostlock_43074
	@echo "=== ghostlock binary ready: ./ghostlock ==="
	@echo "=== CVE-2026-43074 engine ready: ./ghostlock_43074 ==="
	@echo "构建 APK: .\gradlew.bat :app:assembleDebug"

clean:
	rm -f ghostlock ghostlock_43074
