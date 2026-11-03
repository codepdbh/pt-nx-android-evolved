#include "engine/core/resource_path.h"

#include <SDL3/SDL.h>

namespace pt {

std::filesystem::path ExecutableDir() {
#ifdef PT_ANDROID
    // an APK has no folder of plain files beside the program: PtActivity unpacks shaders/ and fonts/ here
    if (const char* internal = SDL_GetAndroidInternalStoragePath()) {
        return std::filesystem::path(internal) / "pt" / "runtime";
    }
#endif
    const char* base = SDL_GetBasePath();
    return base ? std::filesystem::path(reinterpret_cast<const char8_t*>(base)) : std::filesystem::current_path();
}

std::filesystem::path ResourceDir(std::string_view name, const std::filesystem::path& build_dir) {
    std::error_code ec;
    const std::filesystem::path local = ExecutableDir() / name;
    if (std::filesystem::is_directory(local, ec)) {
        return local;
    }
    return build_dir;
}

}
