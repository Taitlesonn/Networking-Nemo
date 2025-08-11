#ifndef OB_GUI
#define OB_GUI

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_ttf.h>
#include <cstdint>
#include <functional>
#include <iostream>
#include <mutex>
#include <sstream>
#include <vector>
#include <string>
#include <algorithm>

#define NO_RUN_ACTION 1
#define RUN_ACTION 0
#define BUTTON_IMG 1
#define DIST_B 10
const char *name_for_img_b = nullptr;

class Image_u {
private:
  SDL_Texture *texture = nullptr;
  int32_t pix_w = 0;
  int32_t pix_h = 0;
  float scaleX = 1.0f, scaleY = 1.0f;

public:
  SDL_Texture *getTexture() const { return texture; }

  void load(const char *path, SDL_Renderer *renderer) {
    SDL_Surface *surface = IMG_Load(path);
    if (!surface) {
      std::cerr << "Błąd ładowania obrazu: " << IMG_GetError() << std::endl;
      return;
    }
    if (texture) {
      SDL_DestroyTexture(texture);
      texture = nullptr;
    }
    texture = SDL_CreateTextureFromSurface(renderer, surface);
    if (texture) {
      if (SDL_QueryTexture(texture, NULL, NULL, &pix_w, &pix_h) != 0) {
        SDL_Log("Błąd przy pobieraniu informacji o teksturze: %s", SDL_GetError());
      }
    }
    SDL_FreeSurface(surface);
  }

  void setScale(float sx, float sy) {
    scaleX = sx;
    scaleY = sy;
  }

  void setSize(int32_t dstW, int32_t dstH) {
    if (pix_w > 0 && pix_h > 0) {
      scaleX = static_cast<float>(dstW) / static_cast<float>(pix_w);
      scaleY = static_cast<float>(dstH) / static_cast<float>(pix_h);
    }
  }

  void Free_i() {
    if (texture != nullptr) {
      SDL_DestroyTexture(texture);
      texture = nullptr;
      pix_w = pix_h = 0;
    }
  }

  void render(SDL_Renderer *renderer, const SDL_Rect &dstRect,
              const SDL_Rect *srcRect = nullptr, double angle = 0.0,
              SDL_Point *center = nullptr,
              SDL_RendererFlip flip = SDL_FLIP_NONE) const {
    if (!texture) return;
    SDL_Rect scaled = {dstRect.x, dstRect.y,
                       static_cast<int>(static_cast<float>(pix_w) * scaleX),
                       static_cast<int>(static_cast<float>(pix_h) * scaleY)};
    SDL_RenderCopyEx(renderer, texture, srcRect, &scaled, angle, center, flip);
  }

  ~Image_u() {
    Free_i();
  }
};


class Button {
private:
  uint32_t x = 0, y = 0, w = 0, h = 0;
  std::string name;             // bezpieczne przechowywanie nazwy
  int8_t type = 0;
  SDL_Rect rect{};
  SDL_Color bgColor{};
  SDL_Color textColor{};
  TTF_Font *font = nullptr;
  SDL_Texture *bgTexture = nullptr;
  Image_u *img = nullptr;
  std::function<void()> on_action;

  // Tekst-caching:
  SDL_Texture* cachedTextTex = nullptr;
  int cachedTextW = 0;
  int cachedTextH = 0;
  int padding = 8;
  int scrollOffset = 0;
  bool cacheValid = false;

public:
  Button(uint32_t _x, uint32_t _y, uint32_t _w, uint32_t _h, int8_t _type,
         const char* _name, TTF_Font* _font = nullptr,
         SDL_Color _textColor = {0,0,0,255},
         SDL_Color _bgColor = {0,0,0,255})
    : x(_x), y(_y), w(_w), h(_h), name(_name ? _name : ""), type(_type),
      rect{static_cast<int>(_x), static_cast<int>(_y),
           static_cast<int>(_w), static_cast<int>(_h)},
      bgColor(_bgColor), textColor(_textColor),
      font(_font), bgTexture(nullptr), img(new Image_u()),
      cachedTextTex(nullptr), cachedTextW(0), cachedTextH(0), scrollOffset(0), cacheValid(false)
  {
      if (!img) {
          std::cerr << "ALLOC ERROR\n";
          std::exit(EXIT_FAILURE);
      }
  }

  // Ustawia tło obrazkowe
  void setBackgroundImg(const char* path, SDL_Renderer* renderer) {
    if (!renderer) return;
    if (img->getTexture() != nullptr) {
      img->Free_i();
    }
    img->load(path, renderer);
    if (img->getTexture()) {
      img->setSize(w, h);
      bgTexture = img->getTexture();
    }
  }

  // Ustaw nową nazwę (z unieważnieniem cache)
  void setName(const std::string& newName) {
    if (name != newName) {
      name = newName;
      invalidateTextCache();
    }
  }

  void setFont(TTF_Font* f) {
    font = f;
    invalidateTextCache();
  }

  void setTextColor(SDL_Color c) {
    textColor = c;
    invalidateTextCache();
  }

  // Tworzy lub odświeża cache tekstu (wywołuj przy zmianie fontu/nazwy)
  void createTextCache(SDL_Renderer* renderer) {
    // usuń istniejący
    if (!font || name.empty()) {
      invalidateTextCache();
      return;
    }

    invalidateTextCache();

    // szerokość o którą zawijamy (wliczając paddingi)
    int wrapW = std::max(1, rect.w - 2*padding);

    // TTF_RenderUTF8_Blended_Wrapped zwraca SDL_Surface z zawiniętym tekstem
    SDL_Surface* surf = TTF_RenderUTF8_Blended_Wrapped(font, name.c_str(), textColor, wrapW);
    if (!surf) {
      std::cerr << "Błąd renderowania tekstu do surface: " << TTF_GetError() << std::endl;
      return;
    }

    // stwórz teksturę z surf
    cachedTextTex = SDL_CreateTextureFromSurface(renderer, surf);
    if (!cachedTextTex) {
      std::cerr << "Błąd tworzenia tekstury z surface: " << SDL_GetError() << std::endl;
      SDL_FreeSurface(surf);
      return;
    }

    cachedTextW = surf->w;   // zwykle == wrapW
    cachedTextH = surf->h;
    cacheValid = true;

    SDL_FreeSurface(surf);

    // upewnij scrollOffset w granicach
    clampScroll();
  }

  void invalidateTextCache() {
    if (cachedTextTex) {
      SDL_DestroyTexture(cachedTextTex);
      cachedTextTex = nullptr;
    }
    cachedTextW = cachedTextH = 0;
    cacheValid = false;
    scrollOffset = 0;
  }

  // Scrolluje tekst (dy: dodatnie przewija w dół)
  void scroll(int dy) {
    scrollOffset += dy;
    clampScroll();
  }

  void clampScroll() {
    if (!cacheValid || cachedTextH <= rect.h - 2*padding) {
      scrollOffset = 0;
      return;
    }
    int maxOffset = cachedTextH - (rect.h - 2*padding);
    if (scrollOffset < 0) scrollOffset = 0;
    if (scrollOffset > maxOffset) scrollOffset = maxOffset;
  }

  void render(SDL_Renderer* renderer) {
    if (type == BUTTON_IMG && bgTexture) {
      img->render(renderer, rect);
      return;
    }

    // tło
    SDL_SetRenderDrawColor(renderer, bgColor.r, bgColor.g, bgColor.b, bgColor.a);
    SDL_RenderFillRect(renderer, &rect);

    // tekst
    if (!name.empty() && font) {
      if (!cacheValid) {
        createTextCache(renderer);
      }

      if (cacheValid && cachedTextTex) {
        // obszar źródłowy w teksturze (część renderowanej zawartości)
        SDL_Rect src = {0, scrollOffset, cachedTextW, std::min(cachedTextH - scrollOffset, rect.h - 2*padding)};
        SDL_Rect dst = { rect.x + padding, rect.y + padding, rect.w - 2*padding, src.h };

        // Clipuj render do rect (na wszelki wypadek)
        SDL_RenderSetClipRect(renderer, &rect);
        SDL_RenderCopy(renderer, cachedTextTex, &src, &dst);
        SDL_RenderSetClipRect(renderer, nullptr);

        // jeśli tekstu mniej niż obszar - możesz wyśrodkować pionowo:
        if (cachedTextH < rect.h - 2*padding) {
          int extra = (rect.h - 2*padding - cachedTextH) / 2;
          // narysuj ponownie przesunięty (proste rozwiązanie: już nie konieczne)
          // zostawiamy standardowy dst (tekst na górze + padding)
        }
      } else {
        // fallback: jeśli cache nie powstał — render bez cache (mniej wydajne)
        int lineSkip = TTF_FontLineSkip(font);
        int yOffset = rect.y + padding;
        std::istringstream iss(name);
        std::string line;
        while (std::getline(iss, line) && yOffset + lineSkip <= rect.y + rect.h - padding) {
          SDL_Surface* surf = TTF_RenderUTF8_Blended_Wrapped(font, line.c_str(), textColor, rect.w - 2*padding);
          if (surf) {
            SDL_Texture* tex = SDL_CreateTextureFromSurface(renderer, surf);
            SDL_Rect dst = {rect.x + padding, yOffset, surf->w, surf->h};
            SDL_RenderCopy(renderer, tex, nullptr, &dst);
            SDL_DestroyTexture(tex);
            SDL_FreeSurface(surf);
          }
          yOffset += lineSkip;
        }
      }
    }
  }

  void set_on_Action(std::function<void()> _on_action) {
    on_action = std::move(_on_action);
  }

  void clic(uint16_t _x, uint16_t _y, uint8_t* app_state, std::mutex* mtx) {
    std::lock_guard<std::mutex> lock(*mtx);
    if (_x >= x && _x <= x + w &&
        _y >= y && _y <= y + h &&
        on_action && app_state && *app_state == 0)
    {
      *app_state = NO_RUN_ACTION;
      on_action();
      *app_state = RUN_ACTION;
    }
  }

  // Settery/gettery
  void setBgColor(SDL_Color _col) { bgColor = _col; }
  uint32_t getX() const { return x; }
  uint32_t getY() const { return y; }
  uint32_t getW() const { return w; }
  uint32_t getH() const { return h; }
  int8_t getType() const { return type; }
  const char *getName() const { return name.c_str(); }

  void setRect(uint32_t newX, uint32_t newY, uint32_t newW, uint32_t newH) {
    x = newX; y = newY; w = newW; h = newH;
    rect = {int(newX), int(newY), int(newW), int(newH)};
    if (type == BUTTON_IMG && img->getTexture()) img->setSize(newW, newH);
    invalidateTextCache();
  }

  void setSize(uint32_t newW, uint32_t newH) {
    w = newW; h = newH;
    rect.w = newW; rect.h = newH;
    if (type == BUTTON_IMG && img->getTexture()) img->setSize(newW, newH);
    invalidateTextCache();
  }

  ~Button() {
    if (img) { delete img; img = nullptr; }
    if (cachedTextTex) { SDL_DestroyTexture(cachedTextTex); cachedTextTex = nullptr; }
    // nie zamykamy fontu tutaj (main zarządza fontami)
  }
};

#endif // OB_GUI
