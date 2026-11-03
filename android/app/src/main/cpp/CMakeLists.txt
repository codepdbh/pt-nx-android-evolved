# Android build of the P.T. port: libmain.so, the engine and game, which SDL's Activity loads and whose main() it calls. A project
# of its own, like switch/CMakeLists.txt, so the desktop and Switch builds stay as they are. Configured by android/app/build.gradle.
cmake_minimum_required(VERSION 3.28)
project(pt_android LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
get_filename_component(PT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../../.." ABSOLUTE)

if(NOT PT_SDL_DIR OR NOT PT_DEPS_DIR)
  message(FATAL_ERROR "build through android/app/build.gradle: it fetches SDL (Java and native parts must match) and sets PT_SDL_DIR, PT_DEPS_DIR")
endif()
option(PT_RELEASE_LOCKS "Release game: the loop browser stays locked until the game was finished once" ON)

include(FetchContent)
cmake_policy(SET CMP0169 OLD)
set(FETCHCONTENT_QUIET ON)
# outside android/app/.cxx: one checkout for every variant, and paths short enough for Windows
set(FETCHCONTENT_BASE_DIR "${PT_DEPS_DIR}")
set(FETCHCONTENT_UPDATES_DISCONNECTED ON)

# The same pinned versions as cmake/Dependencies.cmake and switch/CMakeLists.txt
FetchContent_Declare(vkheaders GIT_REPOSITORY https://github.com/KhronosGroup/Vulkan-Headers.git GIT_TAG v1.4.350 GIT_SHALLOW TRUE)
FetchContent_Declare(volk GIT_REPOSITORY https://github.com/zeux/volk.git GIT_TAG 1.4.350 GIT_SHALLOW TRUE)
FetchContent_Declare(vma GIT_REPOSITORY https://github.com/GPUOpen-LibrariesAndSDKs/VulkanMemoryAllocator.git GIT_TAG v3.4.0 GIT_SHALLOW TRUE)
FetchContent_Declare(glm GIT_REPOSITORY https://github.com/g-truc/glm.git GIT_TAG 1.0.3 GIT_SHALLOW TRUE)
FetchContent_Declare(imgui GIT_REPOSITORY https://github.com/ocornut/imgui.git GIT_TAG v1.92.9b-docking GIT_SHALLOW TRUE)
FetchContent_Declare(stb GIT_REPOSITORY https://github.com/nothings/stb.git GIT_TAG master GIT_SHALLOW TRUE)
FetchContent_Declare(lua51 URL https://www.lua.org/ftp/lua-5.1.5.tar.gz
  URL_HASH SHA256=2640fc56a795f29d28ef15e13c34a47e223960b0240e8cb0a82d9b0738695333)
FetchContent_Declare(ogg GIT_REPOSITORY https://github.com/xiph/ogg.git GIT_TAG v1.3.5 GIT_SHALLOW TRUE)
FetchContent_Declare(vorbis GIT_REPOSITORY https://github.com/xiph/vorbis.git GIT_TAG v1.3.7 GIT_SHALLOW TRUE)
FetchContent_Declare(bc7enc URL https://codeload.github.com/richgel999/bc7enc_rdo/zip/b9438627eef73a1157e84201b6fa6eb2ffd6d9f0)
FetchContent_Declare(harfbuzz GIT_REPOSITORY https://github.com/harfbuzz/harfbuzz.git GIT_TAG 14.6.0 GIT_SHALLOW TRUE)
foreach(dep vkheaders volk vma glm imgui stb lua51 ogg vorbis bc7enc harfbuzz)
  FetchContent_Populate(${dep})
endforeach()

# ---- SDL: shared, as SDL's Android integration expects (its JNI entry points must stay exported) -----------------------------
set(SDL_SHARED ON CACHE BOOL "" FORCE)
set(SDL_STATIC OFF CACHE BOOL "" FORCE)
set(SDL_TEST_LIBRARY OFF CACHE BOOL "" FORCE)
set(SDL_EXAMPLES OFF CACHE BOOL "" FORCE)
add_subdirectory("${PT_SDL_DIR}" "${CMAKE_BINARY_DIR}/_deps/sdl3-build" EXCLUDE_FROM_ALL)

# ---- third party code ----------------------------------------------------------------------------------------------------------
file(GLOB LUA51_SOURCES ${lua51_SOURCE_DIR}/src/*.c)
list(FILTER LUA51_SOURCES EXCLUDE REGEX ".*/(lua|luac|print)\\.c$")

# Lua keeps a string's bytes right after its TString header: bionic's FORTIFY takes the header for the whole buffer, and the collector's
# strchr on a weak table's mode aborts the process ("strchr: prevented read past end of buffer"). A false positive, so off for Lua only.
set_source_files_properties(${LUA51_SOURCES} PROPERTIES COMPILE_OPTIONS "-U_FORTIFY_SOURCE")

add_library(pt_thirdparty STATIC
  ${volk_SOURCE_DIR}/volk.c
  ${imgui_SOURCE_DIR}/imgui.cpp
  ${imgui_SOURCE_DIR}/imgui_draw.cpp
  ${imgui_SOURCE_DIR}/imgui_tables.cpp
  ${imgui_SOURCE_DIR}/imgui_widgets.cpp
  ${imgui_SOURCE_DIR}/imgui_demo.cpp
  ${imgui_SOURCE_DIR}/backends/imgui_impl_sdl3.cpp
  ${imgui_SOURCE_DIR}/backends/imgui_impl_vulkan.cpp
  ${LUA51_SOURCES}
  ${bc7enc_SOURCE_DIR}/bc7enc.cpp
  ${harfbuzz_SOURCE_DIR}/src/harfbuzz.cc)
set_source_files_properties(${harfbuzz_SOURCE_DIR}/src/harfbuzz.cc PROPERTIES COMPILE_OPTIONS "-fno-exceptions;-fno-rtti")
target_include_directories(pt_thirdparty PUBLIC
  ${vkheaders_SOURCE_DIR}/include
  ${volk_SOURCE_DIR}
  ${vma_SOURCE_DIR}/include
  ${glm_SOURCE_DIR}
  ${imgui_SOURCE_DIR}
  ${imgui_SOURCE_DIR}/backends
  ${lua51_SOURCE_DIR}/src
  ${stb_SOURCE_DIR}
  ${bc7enc_SOURCE_DIR}
  ${harfbuzz_SOURCE_DIR}/src)
target_compile_definitions(pt_thirdparty PUBLIC VK_NO_PROTOTYPES IMGUI_IMPL_VULKAN_USE_VOLK)
target_compile_options(pt_thirdparty PRIVATE -w)
target_link_libraries(pt_thirdparty PUBLIC SDL3::SDL3)

# Vorbis for the Wwise audio: the codebook library the desktop build downloads (hash checked), the sources compiled directly
set(PT_WWISE_CODEBOOKS_DIR ${CMAKE_BINARY_DIR}/_deps/ww2ogg)
set(PT_WWISE_CODEBOOKS_BIN ${PT_WWISE_CODEBOOKS_DIR}/packed_codebooks_aoTuV_603.bin)
set(PT_WWISE_CODEBOOKS_C ${PT_WWISE_CODEBOOKS_DIR}/wwise_codebooks.c)
if(NOT EXISTS ${PT_WWISE_CODEBOOKS_BIN})
  file(DOWNLOAD https://raw.githubusercontent.com/hcs64/ww2ogg/master/packed_codebooks_aoTuV_603.bin ${PT_WWISE_CODEBOOKS_BIN}
    EXPECTED_HASH SHA256=00a93eab267d281401b1efd54e888a2e183299b9e6c446c48d09f701a89d9d27 STATUS status)
  list(GET status 0 code)
  if(NOT code EQUAL 0)
    file(REMOVE ${PT_WWISE_CODEBOOKS_BIN})
    message(FATAL_ERROR "cannot download the Wwise Vorbis codebook library: ${status}")
  endif()
endif()
if(NOT EXISTS ${PT_WWISE_CODEBOOKS_C} OR ${PT_WWISE_CODEBOOKS_BIN} IS_NEWER_THAN ${PT_WWISE_CODEBOOKS_C})
  file(READ ${PT_WWISE_CODEBOOKS_BIN} hex HEX)
  string(LENGTH "${hex}" hex_length)
  math(EXPR size "${hex_length} / 2")
  string(REGEX REPLACE "([0-9a-f][0-9a-f])" "0x\\1," bytes "${hex}")
  file(WRITE ${PT_WWISE_CODEBOOKS_C}
    "#include <stddef.h>\nconst unsigned char pt_wwise_codebooks[] = {${bytes}};\nconst size_t pt_wwise_codebooks_size = ${size};\n")
endif()
file(WRITE "${CMAKE_BINARY_DIR}/generated/ogg-config/ogg/config_types.h"
  "#ifndef __CONFIG_TYPES_H__\n#define __CONFIG_TYPES_H__\n#include <stdint.h>\ntypedef int16_t ogg_int16_t;\ntypedef uint16_t ogg_uint16_t;\ntypedef int32_t ogg_int32_t;\ntypedef uint32_t ogg_uint32_t;\ntypedef int64_t ogg_int64_t;\ntypedef uint64_t ogg_uint64_t;\n#endif\n")
set(VORBIS_SOURCES)
foreach(name mdct smallft block envelope window lsp lpc analysis synthesis psy info floor1 floor0 res0 mapping0 registry codebook
    sharedbook lookup bitrate)
  list(APPEND VORBIS_SOURCES ${vorbis_SOURCE_DIR}/lib/${name}.c)
endforeach()
add_library(pt_vorbis STATIC ${ogg_SOURCE_DIR}/src/bitwise.c ${ogg_SOURCE_DIR}/src/framing.c ${VORBIS_SOURCES} ${PT_WWISE_CODEBOOKS_C})
target_include_directories(pt_vorbis PUBLIC ${ogg_SOURCE_DIR}/include ${vorbis_SOURCE_DIR}/include ${CMAKE_BINARY_DIR}/generated/ogg-config
  PRIVATE ${vorbis_SOURCE_DIR}/lib)
target_compile_options(pt_vorbis PRIVATE -w)

# ---- generated headers (as ../../../../../CMakeLists.txt makes them) -----------------------------------------------------------
set(PT_VERSION "1.0.0")
set(PT_UPDATE_MANIFEST_URL "")  # no update check on Android: releases of the PC port are not for this platform
execute_process(COMMAND ${CMAKE_COMMAND} "-DOUT=${CMAKE_BINARY_DIR}/generated/pt_version.h" "-DPT_VERSION=${PT_VERSION}"
  "-DPT_UPDATE_MANIFEST_URL=${PT_UPDATE_MANIFEST_URL}" -P "${PT_ROOT}/cmake/version.cmake")
execute_process(COMMAND ${CMAKE_COMMAND} "-DOUT=${CMAKE_BINARY_DIR}/generated/build_id.h" "-DSOURCE=${PT_ROOT}" -P "${PT_ROOT}/cmake/build_id.cmake")

# ---- the engine ----------------------------------------------------------------------------------------------------------------
file(GLOB_RECURSE ENGINE_SOURCES CONFIGURE_DEPENDS ${PT_ROOT}/src/engine/*.cpp)
file(GLOB_RECURSE GAME_SOURCES CONFIGURE_DEPENDS ${PT_ROOT}/src/game/*.cpp)
# replaced by src/platform/android/android_stubs.cpp, the same set the Switch build replaces: desktop only subsystems, none of which
# a run needs (each reports "not available", which the engine handles)
set(EXCLUDED
  engine/xr/xr_host.cpp
  engine/voice/microphone.cpp engine/voice/voice_recognizer.cpp
  engine/platform/http.cpp engine/platform/os.cpp engine/platform/virtual_pad.cpp engine/platform/livesplit.cpp
  engine/render/upscale/dlss_backend.cpp engine/render/upscale/frame_generation.cpp engine/render/upscale/fsr_backend.cpp
  engine/render/upscale/streamline.cpp engine/render/upscale/xess_backend.cpp engine/render/upscale/upscale_platform.cpp)
set(SOURCES ${ENGINE_SOURCES} ${GAME_SOURCES})
foreach(path ${EXCLUDED})
  list(REMOVE_ITEM SOURCES ${PT_ROOT}/src/${path})
endforeach()
list(APPEND SOURCES ${PT_ROOT}/src/main.cpp ${PT_ROOT}/src/platform/android/android_stubs.cpp)

add_library(main SHARED ${SOURCES})
target_include_directories(main PRIVATE ${PT_ROOT}/src ${CMAKE_BINARY_DIR}/generated)
# the file locations are decided at run time (ExecutableDir, src/engine/core/resource_path.cpp): these are never reached
target_compile_definitions(main PRIVATE PT_ANDROID=1 NOMINMAX GLM_FORCE_DEPTH_ZERO_TO_ONE GLM_FORCE_RADIANS PT_GAMEPLUS=1
  PT_SHADER_DIR="shaders" PT_FONT_DIR="fonts" PT_VOICE_MODEL_DIR="voice")
if(PT_RELEASE_LOCKS)
  target_compile_definitions(main PRIVATE PT_RELEASE_LOCKS=1)
endif()
target_link_libraries(main PRIVATE pt_thirdparty pt_vorbis SDL3::SDL3 android log dl z)
target_link_options(main PRIVATE -Wl,--no-undefined)
