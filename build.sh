#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'
trap 'echo "Błąd w linii $LINENO. Skrypt przerwany." >&2' ERR

log(){ echo -e "\e[1;34m[INFO]\e[0m $*"; }
error(){ echo -e "\e[1;31m[ERROR]\e[0m $*" >&2; exit 1; }

CC="g++"
C_FLAGS=( -O3 -Wextra -Wconversion -Wshadow -Wall -Wnull-dereference -Wunused -Wformat=2 -Wduplicated-cond -Wduplicated-branches -Wlogical-op -Wcast-align -Wuseless-cast -Wpedantic -Wfloat-equal -std=c++17)
VERSION=1.0

for prog in mvn g++ wget unzip autoconf automake libtool make cmake pkg-config java javac; do
    command -v $prog >/dev/null || error "Brak: $prog. Zainstaluj i spróbuj ponownie"
done

PR_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

LIB_DIR="$PR_DIR/lib"
if [ ! -d $LIB_DIR ]; then
    mkdir -p $LIB_DIR
fi

# Biblioteki c++
SDL_LINUX_SRC="$LIB_DIR/SDL2-src"
SDL_LINUX_PREFIX="$LIB_DIR/sdl2-linux"
SDL_TTF_LINUX_SRC="$LIB_DIR/SDL2_ttf-src"
SDL_TTF_PREFIX="$LIB_DIR/sdl2-ttf-linux"
SDL_IMAGE_VERSION=2.6.2
SDL_IMAGE_SRC="LIB_DIR/SDL2_image-src"
SDL_IMAGE_PREFIX="$LIB_DIR/sdl2-image-linux"

SDL_WIN_DIR="$LIB_DIR/sdl2-mingw"
SDL_TTF_WIN_DIR="$LIB_DIR/sdl2-ttf-mingw"
SDL_IMAGE_WIN_DIR="$LIB_DIR/sdl2-image-mingw"

OUT_LINUX="$PR_DIR/out/Linux"
OUT_WINDOWS="PR_DIR/out/Windows"

BUILD_LINUX=false
BUILD_WINDOWS=false
CLEAR_ALL=false
GITHUB_P=false
RPM=false

usage(){
cat << EOF
Use of build.sh <flags>
    --Linux : Build for Linux. (installer)
    --Windows : Build for Windows. (installer)
    --Github : Comit to repo
    --rpm : build RMP
    --clear : clear  ( ALL - { --clear } = false ) 
    --help : this mesage
EOF
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --Linux) BUILD_LINUX=true; shift ;;
        --Windows) BUILD_WINDOWS=true; shift ;;
        --rpm) RPM=true; shift ;;
        --Github) GITHUB_P=true; shift;;
        --clear) CLEAR_ALL=true; shift;;
        --help) usage; exit 0;;
        *)          error "Use --help for more info" ;;
    esac
done

if $CLEAR_ALL; then
  log "Czyszczenie"
  rm -fr $LIB_DIR/*
  exit 0;
fi

if  $BUILD_LINUX  && ! $RPM; then
    mkdir -p $OUT_LINUX
fi

if $BUILD_WINDOWS ; then
    mkdir -p $OUT_WINDOWS
fi

# Windows Static
if $BUILD_WINDOWS ; then
  log "Przygotowanie SDL2 dla Windows..."
  cd "$LIB_DIR"
  rm -rf SDL2-2.30.2 sdl2-mingw sdl2-mingw.zip
  if [ ! -d sdl2-mingw ]; then
      wget -q https://github.com/libsdl-org/SDL/releases/download/release-2.30.2/SDL2-devel-2.30.2-mingw.zip -O sdl2-mingw.zip
      unzip -q sdl2-mingw.zip
      mkdir -p sdl2-mingw/include sdl2-mingw/lib
      mv SDL2-2.30.2/x86_64-w64-mingw32/include/* sdl2-mingw/include
      mv SDL2-2.30.2/x86_64-w64-mingw32/lib/*     sdl2-mingw/lib
      rm -rf SDL2-2.30.2 sdl2-mingw.zip
      log "SDL2 Windows rozpakowany do $SDL_WIN_DIR"
  fi

  if [ -d ! "$SDL_IMAGE_WIN_DIR" ]; then
      log "SDL2_image dla Windows (Mingw)…"
      cd "$LIB_DIR"
      rm -rf SDL2_image-devel-${SDL_IMAGE_VERSION}-mingw.zip sdl2-image-mingw
      wget -q \
          https://github.com/libsdl-org/SDL_image/releases/download/release-${SDL_IMAGE_VERSION}/SDL2_image-devel-${SDL_IMAGE_VERSION}-mingw.zip \
          -O SDL2_image-devel-${SDL_IMAGE_VERSION}-mingw.zip
      unzip -q SDL2_image-devel-${SDL_IMAGE_VERSION}-mingw.zip
      mkdir -p sdl2-image-mingw/include/SDL2 sdl2-image-mingw/lib
      mv SDL2_image-${SDL_IMAGE_VERSION}/x86_64-w64-mingw32/include/SDL2/*.h \
          sdl2-image-mingw/include/SDL2/
      mv SDL2_image-${SDL_IMAGE_VERSION}/x86_64-w64-mingw32/lib/*.a \
          sdl2-image-mingw/lib/
      rm -rf SDL2_image-${SDL_IMAGE_VERSION} SDL2_image-devel-${SDL_IMAGE_VERSION}-mingw.zip
      log "✅ SDL2_image Windows rozpakowany do $SDL_IMAGE_WIN_DIR"
  fi

  # — SDL2_ttf Windows (statyczne) —
  log "Przygotowanie SDL2_ttf dla Windows..."
  cd "$LIB_DIR"
  rm -rf SDL2_ttf-2.20.2 sdl2-ttf-mingw SDL2_ttf-2.20.2-mingw.zip
  if [ ! -d sdl2-ttf-mingw ]; then
      wget -q https://github.com/libsdl-org/SDL_ttf/releases/download/release-2.20.2/SDL2_ttf-devel-2.20.2-mingw.zip \
           -O SDL2_ttf-2.20.2-mingw.zip
      unzip -q SDL2_ttf-2.20.2-mingw.zip
      mkdir -p sdl2-ttf-mingw/include sdl2-ttf-mingw/lib
      mv SDL2_ttf-2.20.2/x86_64-w64-mingw32/include/*  sdl2-ttf-mingw/include
      mv SDL2_ttf-2.20.2/x86_64-w64-mingw32/lib/*      sdl2-ttf-mingw/lib
      rm -rf SDL2_ttf-2.20.2 SDL2_ttf-2.20.2-mingw.zip
  fi
  cd "$PR_DIR"
  log "✅ SDL2_ttf Windows rozpakowany do $LIB_DIR/sdl2-ttf-mingw"
fi

if $BUILD_LINUX; then
    if [ ! -d "$SDL_LINUX_PREFIX" ]; then
      log "Pobieranie źródeł SDL2..."
      cd "$LIB_DIR"
      rm -rf SDL2-src SDL2-2.30.2.tar.gz
      wget -q https://github.com/libsdl-org/SDL/releases/download/release-2.30.2/SDL2-2.30.2.tar.gz
      tar xf SDL2-2.30.2.tar.gz
      mv SDL2-2.30.2 SDL2-src
      mkdir -p SDL2-src/build && cd SDL2-src/build
      log "Konfiguracja SDL2 (--disable-shared --enable-static, wymuszam --libdir)"
      ../configure \
          --prefix="$SDL_LINUX_PREFIX" \
          --libdir="$SDL_LINUX_PREFIX/lib" \
          --disable-shared \
          --enable-static
      log "Kompilacja SDL2..."
      make -j$(nproc)
      make install
      cd "$LIB_DIR"
      rm -rf SDL2-2.30.2.tar.gz SDL2-src
      cd "$PR_DIR"
      log "SDL2 Linux zainstalowany do $SDL_LINUX_PREFIX"
    fi
      if [ ! -d "$SDL_TTF_PREFIX" ]; then
        log "Pobieranie źródeł SDL2_ttf..."
        cd "$LIB_DIR"
        rm -rf SDL2_ttf-src SDL2_ttf-2.20.2.tar.gz
        wget -q https://github.com/libsdl-org/SDL_ttf/releases/download/release-2.20.2/SDL2_ttf-2.20.2.tar.gz
        tar xf SDL2_ttf-2.20.2.tar.gz
        mv SDL2_ttf-2.20.2 SDL2_ttf-src
        mkdir -p SDL2_ttf-src/build && cd SDL2_ttf-src/build

        log "Konfiguracja SDL2_ttf (--disable-shared --enable-static)"
        ../configure \
            --prefix="$SDL_TTF_PREFIX" \
            --libdir="$SDL_TTF_PREFIX/lib" \
            --disable-shared \
            --enable-static \
            --with-sdl-prefix="$SDL_LINUX_PREFIX" \
            --with-freetype-prefix=/usr

        log "Kompilacja SDL2_ttf..."
        make -j$(nproc)
        make install
        cd "$LIB_DIR"
        rm -rf SDL2_ttf-2.20.2.tar.gz SDL2_ttf-src
        cd "$PR_DIR"
        log "✅ SDL2_ttf Linux zainstalowany do $SDL_TTF_PREFIX"
      fi

      if [ ! -d "$SDL_IMAGE_PREFIX" ]; then
        log "Pobieranie SDL2_image źródła..."
        cd "$LIB_DIR"
        wget -q https://github.com/libsdl-org/SDL_image/releases/download/release-${SDL_IMAGE_VERSION}/SDL2_image-${SDL_IMAGE_VERSION}.tar.gz
        tar xf SDL2_image-${SDL_IMAGE_VERSION}.tar.gz
        mv SDL2_image-${SDL_IMAGE_VERSION} SDL2_image-src
        mkdir -p SDL2_image-src/build && cd SDL2_image-src/build
        ../configure \
          --prefix="$SDL_IMAGE_PREFIX" \
          --libdir="$SDL_IMAGE_PREFIX/lib" \
          --disable-shared \
          --enable-static \
          --with-sdl-prefix="$SDL_LINUX_PREFIX"
        make -j$(nproc) && make install
        cd "$LIB_DIR"
        rm -rf SDL2_image-${SDL_IMAGE_VERSION}.tar.gz SDL2_image-src
        cd "$PR_DIR"
        log "SDL2_image Linux zainstalowany"
      fi

fi

if $BUILD_LINUX; then
  log "Kompilacja aplikacji dla Linuxa (statyczne SDL2 + SDL2_ttf)..."
  "$CC" "${C_FLAGS[@]}" \
    -I"$SDL_LINUX_PREFIX/include/SDL2" \
    -I"$SDL_TTF_PREFIX/include" \
    src/main/c/Main.cpp \
    -o "$OUT_LINUX/Installer" \
    "$SDL_LINUX_PREFIX/lib/libSDL2.a" \
    "$SDL_TTF_PREFIX/lib/libSDL2_ttf.a" \
    "$SDL_IMAGE_PREFIX/lib/libSDL2_image.a" \
    -lfreetype \
    -ldl \
    -lpthread \
    -lm \
    -ljpeg \
    -lpng \
    -lwebp \
    -ljxl
  log "✅ Zbudowano: $OUT_LINUX/installer"
  mkdir -p "$OUT_LINUX/fonts"
  mkdir -p "$OUT_LINUX/images"
  rsync -av --delete "$PR_DIR/src/main/c/images/" "$OUT_LINUX/images/"
  rsync -av --delete "$PR_DIR/src/main/c/fonts/" "$OUT_LINUX/fonts/"
fi

# — Kompilacja aplikacji dla Windowsa (statyczne SDL2 + SDL2_ttf + SDL2_image) —
if $BUILD_WINDOWS; then
  log "Kompilacja aplikacji dla Windowsa (statyczne SDL2 + SDL2_ttf + SDL2_image)…"
  cd "$PR_DIR"
  x86_64-w64-mingw32-g++ "${C_FLAGS[@]}" \
    -static -static-libgcc -static-libstdc++ \
    -I"$SDL_WIN_DIR/include" \
    -I"$SDL_WIN_DIR/include/SDL2" \
    -I"$LIB_DIR/sdl2-ttf-mingw/include" \
    -I"$LIB_DIR/sdl2-image-mingw/include" \
    -I"$LIB_DIR/sdl2-image-mingw/include/SDL2" \
    -L"$SDL_WIN_DIR/lib" \
    -L"$LIB_DIR/sdl2-ttf-mingw/lib" \
    -L"$LIB_DIR/sdl2-image-mingw/lib" \
    src/main/c/Main.cpp \
    -o "$OUT_WINDOWS/Installer.exe" \
    -lmingw32 \
    -lSDL2main \
    -lSDL2 \
    -lSDL2_ttf \
    -lSDL2_image \
    -lws2_32 \
    -lole32 \
    -loleaut32 \
    -luuid \
    -lrpcrt4 \
    -limm32 \
    -lwinmm \
    -lgdi32 \
    -lsetupapi \
    -lcfgmgr32 \
    -lversion \
    -mwindows

  log "✅ Zbudowano: $OUT_WINDOWS/app.exe"
  mkdir -p "$OUT_WINDOWS/fonts" "$OUT_WINDOWS/images"
  rsync -av --delete "$PR_DIR/src/main/c/images/" "$OUT_WINDOWS/images/"
  rsync -av --delete "$PR_DIR/src/main/c/fonts/"  "$OUT_WINDOWS/fonts/"
EOF
fi


# rpm w trakcie  