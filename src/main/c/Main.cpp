#include "Ob_Gui.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_ttf.h>
#include <atomic>
#include <cstdint>
#include <memory>
#include <iostream>
#include <string>
#include <mutex>
#include <vector>

// Kolory
const SDL_Color background = { 240, 248, 255, 255 }; // jasny pastelowy błękit
const SDL_Color textColor = { 30, 30, 30, 255 };     // ciemny szary dla tekstu
const SDL_Color buttonColor = { 0, 128, 0, 255 };    // zielony
const SDL_Color buttonHoverColor = { 34, 139, 34, 255 }; // ciemniejszy zielony
const SDL_Color headerColor = { 70, 130, 180, 255 }; // steel blue

// Angielska licencja (rozbita na krótsze linie)
const char *LICENSE_EN = R"(
END USER LICENSE AGREEMENT (EULA); github: Taitlesonn

1. Definitions
"Software" refers to Networking-nemo, including all source
code, binaries, documentation, and any related materials
provided by the Licensor.
"Licensor" refers to Michał Skoczylas.
"Licensee" refers to any individual or entity using the
Software.

2. Grant of License
The Licensor grants the Licensee a non-exclusive,
non-transferable, royalty-free license to use and
distribute the Software in its original, unmodified form,
free of charge, for any purpose, including personal and
commercial use.

3. Restrictions
The Licensee may not, without prior written permission from
the Licensor:
a) Modify, alter, adapt, or translate the Software,
b) Create derivative works based on the Software,
c) Distribute modified versions of the Software,
d) Remove or alter any copyright notices or proprietary
   markings.

4. Copyright
The Software is protected by copyright law and international
treaties. All rights not expressly granted in this
Agreement are reserved by the Licensor.

5. Disclaimer of Warranty
The Software is provided "as is" without any warranty of
any kind, express or implied. The Licensor shall not be
held liable for any damages, direct or indirect, arising
from the use or inability to use the Software.

6. Termination
This Agreement is effective until terminated. It will
terminate automatically without notice if the Licensee
fails to comply with any of its terms. Upon termination,
the Licensee must cease all use and distribution of the
Software and delete all copies in their possession.

7. Governing Law
This Agreement shall be governed by and construed in
accordance with the laws of Poland. Any disputes arising
under this Agreement shall be subject to the exclusive
jurisdiction of the courts located in Poland.

)";


const char *LICENSE_PL = R"(
UMOWA LICENCYJNA DLA UŻYTKOWNIKA (EULA); github: Taitlesonn

1. Definicje
"Software" odnosi się do Networking-nemo, włączając cały kod
źródłowy, binaria, dokumentację oraz powiązane materiały
udostępnione przez Licencjodawcę.
"Licencjodawca" oznacza Michała Skoczylasa.
"Licencjobiorca" oznacza każdą osobę lub podmiot korzystający
z Oprogramowania.

2. Udzielenie licencji
Licencjodawca udziela Licencjobiorcy niewyłącznej,
nietykalnej, bez tantiem licencji na używanie i dystrybucję
Oprogramowania w jego oryginalnej, niezmienionej formie,
bezpłatnie, do dowolnego celu, w tym komercyjnego.

3. Ograniczenia
Licencjobiorca nie może, bez uprzedniej pisemnej zgody
Licencjodawcy:
a) Modyfikować, zmieniać, adaptować lub tłumaczyć
   Oprogramowania,
b) Tworzyć utworów zależnych na podstawie Oprogramowania,
c) Dystrybuować zmodyfikowanych wersji Oprogramowania,
d) Usuwać lub zmieniać informacji o prawach autorskich lub
   oznaczeń własnościowych.

4. Prawa autorskie
Oprogramowanie jest chronione prawem autorskim i traktatami
międzynarodowymi. Wszystkie prawa niewyraźnie udzielone w
niniejszej Umowie zastrzeżone są przez Licencjodawcę.

5. Wyłączenie gwarancji
Oprogramowanie dostarczane jest "tak jak jest" bez żadnej
gwarancji. Licencjodawca nie ponosi odpowiedzialności za
jakiekolwiek szkody wynikłe z używania Oprogramowania.

6. Rozwiązanie umowy
Niniejsza Umowa obowiązuje do czasu jej rozwiązania. Zakończy
się automatycznie, jeżeli Licencjobiorca nie będzie przestrzegać
jej warunków. Po rozwiązaniu Licencjobiorca musi zaprzestać
użytkowania i usunąć wszystkie kopie Oprogramowania.

7. Prawo właściwe
Umowa podlega prawu Polskiemu. Wszelkie spory będą rozstrzygane
przez sądy właściwe w Polsce.
)";


struct SDLWindowDeleter {
    void operator()(SDL_Window* ptr) const { if (ptr) SDL_DestroyWindow(ptr); }
};

struct SDLRendererDeleter {
    void operator()(SDL_Renderer* ptr) const { if (ptr) SDL_DestroyRenderer(ptr); }
};

struct TTF_FontDeleter {
    void operator()(TTF_Font* ptr) const { if (ptr) TTF_CloseFont(ptr); }
};


void renderTextWrapped(SDL_Renderer* renderer, TTF_Font* font, const std::string& text, SDL_Color color, SDL_Rect area) {
    std::istringstream iss(text);
    std::string line;
    int lineHeight = TTF_FontLineSkip(font);
    int y = area.y + 6; 

    while (std::getline(iss, line) && y + lineHeight < area.y + area.h - 6) {

        SDL_Surface* surf = TTF_RenderUTF8_Blended_Wrapped(font, line.c_str(), color, area.w - 12);
        if (surf) {
            SDL_Texture* texture = SDL_CreateTextureFromSurface(renderer, surf);
            SDL_Rect dst = {area.x + 6, y, surf->w, surf->h};
            SDL_RenderCopy(renderer, texture, nullptr, &dst);
            SDL_DestroyTexture(texture);
            SDL_FreeSurface(surf);
        }
        y += lineHeight;
    }
}

int main() {
    std::atomic<bool> running = true;
    const uint16_t win_w = 700;
    const uint16_t win_h = 900;

    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        std::cerr << "Błąd SDL_Init: " << SDL_GetError() << std::endl;
        return 1;
    }

    if (!(IMG_Init(IMG_INIT_JPG) & IMG_INIT_JPG)) {
        std::cerr << "Błąd IMG_Init: " << IMG_GetError() << std::endl;
        SDL_Quit();
        return 1;
    }

    std::unique_ptr<SDL_Window, SDLWindowDeleter> window(SDL_CreateWindow(
        "Nemo Installer",
        SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED,
        win_w, win_h, SDL_WINDOW_SHOWN
    ));

    if (!window) {
        std::cerr << "Błąd tworzenia okna: " << SDL_GetError() << std::endl;
        IMG_Quit();
        SDL_Quit();
        return 1;
    }

    SDL_SetWindowResizable(window.get(), SDL_FALSE);

    std::unique_ptr<SDL_Renderer, SDLRendererDeleter> renderer(SDL_CreateRenderer(window.get(), -1, SDL_RENDERER_ACCELERATED));
    if (!renderer) {
        std::cerr << "Błąd tworzenia renderera: " << SDL_GetError() << std::endl;
        IMG_Quit();
        SDL_Quit();
        return 1;
    }

    if (TTF_Init() == -1) {
        std::cerr << "Błąd TTF_Init: " << TTF_GetError() << std::endl;
        IMG_Quit();
        SDL_Quit();
        return 1;
    }

    std::unique_ptr<TTF_Font, TTF_FontDeleter> fontHeader(TTF_OpenFont("fonts/Arial.ttf", 28));
    std::unique_ptr<TTF_Font, TTF_FontDeleter> fontText(TTF_OpenFont("fonts/Arial.ttf", 12));
    if (!fontHeader || !fontText) {
        std::cerr << "Błąd ładowania fontu Arial: " << TTF_GetError() << std::endl;
        TTF_Quit();
        IMG_Quit();
        SDL_Quit();
        return 1;
    }


    std::string activeLicense = LICENSE_EN;
    bool isPolish = false;

 
    bool acceptHovered = false;
    bool toggleHovered = false;


    SDL_Rect toggleRect = {win_w - 160, win_h - 140, 140, 40};
    SDL_Rect btnRect = {win_w / 2 - 70, win_h - 80, 140, 50};


    SDL_Rect licenseArea = {20, 80, win_w - 40, win_h - 180};

    SDL_Event evn;

    while (running) {

        while (SDL_PollEvent(&evn)) {
            switch (evn.type) {
                case SDL_QUIT:
                    running = false;
                    break;

                case SDL_MOUSEMOTION: {
                    int mx = evn.motion.x;
                    int my = evn.motion.y;
                    acceptHovered = (mx >= btnRect.x && mx <= btnRect.x + btnRect.w && my >= btnRect.y && my <= btnRect.y + btnRect.h);
                    toggleHovered = (mx >= toggleRect.x && mx <= toggleRect.x + toggleRect.w && my >= toggleRect.y && my <= toggleRect.y + toggleRect.h);
                    break;
                }

                case SDL_MOUSEBUTTONDOWN: {
                    int mx = evn.button.x;
                    int my = evn.button.y;
                    if (mx >= btnRect.x && mx <= btnRect.x + btnRect.w && my >= btnRect.y && my <= btnRect.y + btnRect.h) {
                        std::cout << "Akceptuję licencję i rozpoczynam instalację...\n";
                        // TODO: akcja instalacji
                        running = false;
                    } else if (mx >= toggleRect.x && mx <= toggleRect.x + toggleRect.w && my >= toggleRect.y && my <= toggleRect.y + toggleRect.h) {
                        isPolish = !isPolish;
                        activeLicense = isPolish ? LICENSE_PL : LICENSE_EN;
                    }
                    break;
                }

                case SDL_MOUSEWHEEL: {

                    break;
                }
            }
        }

        SDL_SetRenderDrawColor(renderer.get(), background.r, background.g, background.b, background.a);
        SDL_RenderClear(renderer.get());


        {
            SDL_Surface* surf = TTF_RenderUTF8_Blended(fontHeader.get(), "Nemo Installer - Licencja", headerColor);
            if (surf) {
                SDL_Texture* tex = SDL_CreateTextureFromSurface(renderer.get(), surf);
                SDL_Rect dst = {20, 20, surf->w, surf->h};
                SDL_RenderCopy(renderer.get(), tex, nullptr, &dst);
                SDL_DestroyTexture(tex);
                SDL_FreeSurface(surf);
            }
        }


        {
            SDL_SetRenderDrawColor(renderer.get(), 255, 255, 255, 255);
            SDL_RenderFillRect(renderer.get(), &licenseArea);
            SDL_SetRenderDrawColor(renderer.get(), 200, 200, 200, 255);
            SDL_RenderDrawRect(renderer.get(), &licenseArea);

            renderTextWrapped(renderer.get(), fontText.get(), activeLicense, textColor, licenseArea);
        }


        {
            SDL_Color cur = toggleHovered ? buttonHoverColor : buttonColor;
            SDL_SetRenderDrawColor(renderer.get(), cur.r, cur.g, cur.b, cur.a);
            SDL_RenderFillRect(renderer.get(), &toggleRect);
            SDL_SetRenderDrawColor(renderer.get(), 0, 0, 0, 255);
            SDL_RenderDrawRect(renderer.get(), &toggleRect);

            const char* label = "English / Polish";
            SDL_Surface* surf = TTF_RenderUTF8_Blended(fontText.get(), label, {255,255,255,255});
            if (surf) {
                SDL_Texture* tex = SDL_CreateTextureFromSurface(renderer.get(), surf);
                SDL_Rect dst = { toggleRect.x + (toggleRect.w - surf->w)/2, toggleRect.y + (toggleRect.h - surf->h)/2, surf->w, surf->h };
                SDL_RenderCopy(renderer.get(), tex, nullptr, &dst);
                SDL_DestroyTexture(tex);
                SDL_FreeSurface(surf);
            }
        }

        {
            SDL_Color cur = acceptHovered ? buttonHoverColor : buttonColor;
            SDL_SetRenderDrawColor(renderer.get(), cur.r, cur.g, cur.b, cur.a);
            SDL_RenderFillRect(renderer.get(), &btnRect);
            SDL_SetRenderDrawColor(renderer.get(), 0, 0, 0, 255);
            SDL_RenderDrawRect(renderer.get(), &btnRect);

            SDL_Surface* surf = TTF_RenderUTF8_Blended(fontText.get(), "Akceptuj", {255,255,255,255});
            if (surf) {
                SDL_Texture* tex = SDL_CreateTextureFromSurface(renderer.get(), surf);
                SDL_Rect dst = { btnRect.x + (btnRect.w - surf->w)/2, btnRect.y + (btnRect.h - surf->h)/2, surf->w, surf->h };
                SDL_RenderCopy(renderer.get(), tex, nullptr, &dst);
                SDL_DestroyTexture(tex);
                SDL_FreeSurface(surf);
            }
        }

        SDL_RenderPresent(renderer.get());
        SDL_Delay(16);
    }

    fontHeader.reset();
    fontText.reset();

    // Sprzątanie
    TTF_Quit();
    IMG_Quit();
    SDL_Quit();
    return 0;
}
