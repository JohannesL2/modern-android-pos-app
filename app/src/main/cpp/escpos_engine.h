#ifndef ESCPOS_ENGINE_H
#define ESCPOS_ENGINE_H

#include <vector>
#include <cstdint>
#include <span>

namespace EscPosCore {
    std::vector<uint8_t> prepareRasterData(std::span<const int32_t> pixels, int width, int height);
}

#endif