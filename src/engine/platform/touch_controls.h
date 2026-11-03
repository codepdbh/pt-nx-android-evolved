#pragma once

#include <glm/glm.hpp>

#include <cstdint>
#include <vector>

union SDL_Event;
struct ImDrawList;

namespace pt {

// What the on-screen controls hold this frame, in the terms of a gamepad (InputDevice::Poll merges it with the real pads).
struct TouchInput {
    glm::vec2 move{0.0f};  // x right, y forward, length up to 1
    glm::vec2 look{0.0f};  // window pixels dragged since the last frame
    uint32_t raw = 0;      // PadRaw bits
    bool voice = false;    // the word of the final puzzle, once per press
};

// The Android on-screen controls. In play: a floating joystick on the left half of the screen, a camera drag on the right half, and buttons.
// In menus: a virtual pad; a tap anywhere else stays a mouse click, which the menus already handle.
class TouchControls {
public:
    enum class Mode { Off, Play, Menu };

    void SetMode(Mode mode);
    Mode GetMode() const { return mode_; }
    void SetVoiceAvailable(bool available) { voice_available_ = available; }
    // the window's size and the part of it clear of the camera cutout and system bars
    void SetViewport(float width, float height, float left, float top, float right, float bottom);

    void ProcessEvent(const SDL_Event& event);
    bool HitsButton(float x, float y) const;  // window coordinates
    TouchInput Take();
    void Draw(ImDrawList* draw) const;

private:
    enum class Icon { Cross, Circle, Square, Triangle, Up, Down, Left, Right, Zoom, Pause, Start, Voice, L1, R1 };
    struct Button {
        Icon icon;
        glm::vec2 center;
        float radius;
        uint32_t raw;
    };
    struct Finger {
        int64_t id;
        int button;  // index into buttons_, -1 for the stick or the camera
        bool stick;
        glm::vec2 origin;
        glm::vec2 position;
    };

    void Layout();
    int ButtonAt(glm::vec2 point) const;
    void Release(int64_t id);

    Mode mode_ = Mode::Off;
    bool voice_available_ = false;
    glm::vec2 size_{1.0f};
    glm::vec2 safe_min_{0.0f};
    glm::vec2 safe_max_{1.0f};
    std::vector<Button> buttons_;
    std::vector<Finger> fingers_;
    glm::vec2 look_{0.0f};
    uint32_t tapped_raw_ = 0;  // pressed and released between two frames: still seen once
    bool voice_pressed_ = false;
};

}
