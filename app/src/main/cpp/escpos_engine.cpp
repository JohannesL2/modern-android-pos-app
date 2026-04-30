#include "escpos_engine.h"

namespace EscPosCore {

    std::vector<uint8_t> prepareRasterData(std::span<const int32_t> pixels, int width, int height) {
        const int widthBytes = (width + 7) / 8;
        std::vector<uint8_t> data;
        data.reserve(8 + (widthBytes * height));

        // ESC/POS GS v 0 kommando
        data.push_back(0x1D); data.push_back(0x76); data.push_back(0x30); data.push_back(0);
        data.push_back(static_cast<uint8_t>(widthBytes % 256));
        data.push_back(static_cast<uint8_t>(widthBytes / 256));
        data.push_back(static_cast<uint8_t>(height % 256));
        data.push_back(static_cast<uint8_t>(height / 256));

        for (int y = 0; y < height; ++y) {
            for (int xByte = 0; xByte < widthBytes; ++xByte) {
                uint8_t currentByte = 0;
                for (int bit = 0; bit < 8; ++bit) {
                    int pxX = xByte * 8 + bit;
                    if (pxX < width) {
                        int32_t p = pixels[y * width + pxX];
                        int r = (p >> 16) & 0xFF;
                        int g = (p >> 8) & 0xFF;
                        int b = p & 0xFF;
                        if ((r * 0.299f + g * 0.587f + b * 0.114f) < 128) {
                            currentByte |= (1 << (7 - bit));
                        }
                    }
                }
                data.push_back(currentByte);
            }
        }
        return data;
    }

}