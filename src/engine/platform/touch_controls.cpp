#ifdef PT_ANDROID

#include "engine/platform/touch_controls.h"

#include <SDL3/SDL.h>
#include <imgui.h>

#include <algorithm>

#include "engine/platform/input.h"

namespace pt {
namespace {

constexpr float kScale = 0.72f;        // the controls' sizes below are shares of the screen's short side, times this
constexpr float kStickRadius = 0.14f * kScale;
constexpr float kStickDead = 0.12f;
constexpr uint32_t kVoiceBit = 0;      // the voice button holds no pad button

ImU32 Colour(float alpha) { return IM_COL32(255, 255, 255, static_cast<int>(alpha * 255.0f)); }

}

void TouchControls::SetMode(Mode mode) {
    if (mode == mode_) {
        return;
    }
    mode_ = mode;
    fingers_.clear();  // a finger held across the change must not press what is now under it
    look_ = glm::vec2(0.0f);
    tapped_raw_ = 0;
    voice_pressed_ = false;
    Layout();
}

void TouchControls::SetViewport(float width, float height, float left, float top, float right, float bottom) {
    size_ = glm::vec2(width, height);
    safe_min_ = glm::vec2(left, top);
    safe_max_ = glm::vec2(right, bottom);
    Layout();
}

void TouchControls::Layout() {
    buttons_.clear();
    const float h = std::min(size_.x, size_.y) * kScale;
    const float l = safe_min_.x;
    const float t = safe_min_.y;
    const float r = safe_max_.x;
    const float b = safe_max_.y;
    const auto add = [&](Icon icon, float x, float y, float radius, uint32_t raw) { buttons_.push_back({icon, {x, y}, radius * h, raw}); };
    if (mode_ == Mode::Play) {
        add(Icon::Cross, r - 0.17f * h, b - 0.22f * h, 0.115f, kRawCross);
        add(Icon::Zoom, r - 0.44f * h, b - 0.14f * h, 0.085f, kRawR3);
        add(Icon::Pause, r - 0.09f * h, t + 0.10f * h, 0.06f, kRawStart);
        if (voice_available_) {
            add(Icon::Voice, r - 0.17f * h, b - 0.54f * h, 0.09f, kVoiceBit);
        }
    } else if (mode_ == Mode::Menu) {
        const float pad_x = l + 0.27f * h;
        const float face_x = r - 0.27f * h;
        const float y = b - 0.27f * h;
        const float gap = 0.15f * h;
        const float radius = 0.075f;
        add(Icon::Up, pad_x, y - gap, radius, kRawUp);
        add(Icon::Down, pad_x, y + gap, radius, kRawDown);
        add(Icon::Left, pad_x - gap, y, radius, kRawLeft);
        add(Icon::Right, pad_x + gap, y, radius, kRawRight);
        add(Icon::Cross, face_x, y + gap, radius, kRawCross);
        add(Icon::Circle, face_x + gap, y, radius, kRawCircle);
        add(Icon::Square, face_x - gap, y, radius, kRawSquare);
        add(Icon::Triangle, face_x, y - gap, radius, kRawTriangle);
        add(Icon::L1, l + 0.11f * h, t + 0.10f * h, 0.06f, kRawL1);
        add(Icon::R1, r - 0.11f * h, t + 0.10f * h, 0.06f, kRawR1);
        add(Icon::Start, size_.x * 0.5f, t + 0.08f * h, 0.055f, kRawStart);
    }
}

int TouchControls::ButtonAt(glm::vec2 point) const {
    for (size_t i = 0; i < buttons_.size(); ++i) {
        const float reach = buttons_[i].radius * 1.15f;  // a thumb is not exact
        if (glm::dot(point - buttons_[i].center, point - buttons_[i].center) <= reach * reach) {
            return static_cast<int>(i);
        }
    }
    return -1;
}

bool TouchControls::HitsButton(float x, float y) const { return ButtonAt({x, y}) >= 0; }

void TouchControls::Release(int64_t id) {
    for (size_t i = 0; i < fingers_.size(); ++i) {
        if (fingers_[i].id == id) {
            if (fingers_[i].button >= 0 && fingers_[i].button < static_cast<int>(buttons_.size())) {
                tapped_raw_ |= buttons_[fingers_[i].button].raw;
            }
            fingers_.erase(fingers_.begin() + static_cast<std::ptrdiff_t>(i));
            return;
        }
    }
}

void TouchControls::ProcessEvent(const SDL_Event& event) {
    if (mode_ == Mode::Off) {
        return;
    }
    switch (event.type) {
    case SDL_EVENT_FINGER_DOWN: {
        const int64_t id = static_cast<int64_t>(event.tfinger.fingerID);
        const glm::vec2 point = glm::vec2(event.tfinger.x, event.tfinger.y) * size_;
        Release(id);
        const int button = ButtonAt(point);
        if (button >= 0) {
            if (buttons_[button].icon == Icon::Voice) {
                voice_pressed_ = true;
            }
            fingers_.push_back({id, button, false, point, point});
        } else if (mode_ == Mode::Play) {
            const bool left = point.x < size_.x * 0.5f;
            const bool stick_free = std::none_of(fingers_.begin(), fingers_.end(), [](const Finger& f) { return f.stick; });
            fingers_.push_back({id, -1, left && stick_free, point, point});
        }
        break;
    }
    case SDL_EVENT_FINGER_MOTION: {
        const int64_t id = static_cast<int64_t>(event.tfinger.fingerID);
        for (Finger& finger : fingers_) {
            if (finger.id != id) {
                continue;
            }
            const glm::vec2 point = glm::vec2(event.tfinger.x, event.tfinger.y) * size_;
            if (finger.button < 0 && !finger.stick) {
                look_ += point - finger.position;
            }
            finger.position = point;
        }
        break;
    }
    case SDL_EVENT_FINGER_UP:
    case SDL_EVENT_FINGER_CANCELED:
        Release(static_cast<int64_t>(event.tfinger.fingerID));
        break;
    case SDL_EVENT_WILL_ENTER_BACKGROUND:
        fingers_.clear();
        break;
    default:
        break;
    }
}

TouchInput TouchControls::Take() {
    TouchInput out;
    out.look = look_;
    out.raw = tapped_raw_;
    out.voice = voice_pressed_;
    look_ = glm::vec2(0.0f);
    tapped_raw_ = 0;
    voice_pressed_ = false;
    const float radius = kStickRadius * std::min(size_.x, size_.y);
    for (const Finger& finger : fingers_) {
        if (finger.button >= 0 && finger.button < static_cast<int>(buttons_.size())) {
            out.raw |= buttons_[finger.button].raw;
        } else if (finger.stick && radius > 0.0f) {
            glm::vec2 v = (finger.position - finger.origin) / radius;
            const float length = glm::length(v);
            if (length > kStickDead) {
                v = v / length * std::min(1.0f, (length - kStickDead) / (1.0f - kStickDead));
                out.move = glm::vec2(v.x, -v.y);
            }
        }
    }
    return out;
}

void TouchControls::Draw(ImDrawList* draw) const {
    if (mode_ == Mode::Off || !draw) {
        return;
    }
    const auto point = [](glm::vec2 p) { return ImVec2(p.x, p.y); };
    for (size_t i = 0; i < buttons_.size(); ++i) {
        const Button& b = buttons_[i];
        const bool held = std::any_of(fingers_.begin(), fingers_.end(), [&](const Finger& f) { return f.button == static_cast<int>(i); });
        const float alpha = held ? 0.65f : 0.32f;
        const ImU32 line = Colour(alpha);
        const float thick = std::max(2.0f, b.radius * 0.07f);
        const float s = b.radius * 0.45f;
        const glm::vec2 c = b.center;
        draw->AddCircleFilled(point(c), b.radius, IM_COL32(0, 0, 0, held ? 90 : 45), 48);
        draw->AddCircle(point(c), b.radius, line, 48, thick);
        const auto arrow = [&](glm::vec2 tip, glm::vec2 a, glm::vec2 e) {
            draw->AddTriangleFilled(point(c + tip * s), point(c + a * s), point(c + e * s), line);
        };
        const auto text = [&](const char* label) {
            const float size = b.radius * 0.72f;
            const ImVec2 extent = ImGui::GetFont()->CalcTextSizeA(size, FLT_MAX, 0.0f, label);
            draw->AddText(ImGui::GetFont(), size, ImVec2(c.x - extent.x * 0.5f, c.y - extent.y * 0.5f), line, label);
        };
        switch (b.icon) {
        case Icon::Cross:
            draw->AddLine(point(c + glm::vec2(-s, -s)), point(c + glm::vec2(s, s)), line, thick);
            draw->AddLine(point(c + glm::vec2(-s, s)), point(c + glm::vec2(s, -s)), line, thick);
            break;
        case Icon::Circle: draw->AddCircle(point(c), s, line, 32, thick); break;
        case Icon::Square: draw->AddRect(point(c - glm::vec2(s)), point(c + glm::vec2(s)), line, 0.0f, 0, thick); break;
        case Icon::Triangle:
            draw->AddTriangle(point(c + glm::vec2(0.0f, -s)), point(c + glm::vec2(s, s * 0.8f)), point(c + glm::vec2(-s, s * 0.8f)), line, thick);
            break;
        case Icon::Up: arrow({0.0f, -1.0f}, {-1.0f, 0.7f}, {1.0f, 0.7f}); break;
        case Icon::Down: arrow({0.0f, 1.0f}, {1.0f, -0.7f}, {-1.0f, -0.7f}); break;
        case Icon::Left: arrow({-1.0f, 0.0f}, {0.7f, 1.0f}, {0.7f, -1.0f}); break;
        case Icon::Right: arrow({1.0f, 0.0f}, {-0.7f, -1.0f}, {-0.7f, 1.0f}); break;
        case Icon::Zoom:
            draw->AddCircle(point(c - glm::vec2(s * 0.2f)), s * 0.7f, line, 24, thick);
            draw->AddLine(point(c + glm::vec2(s * 0.3f)), point(c + glm::vec2(s)), line, thick);
            break;
        case Icon::Pause:
            draw->AddRectFilled(point(c + glm::vec2(-s * 0.7f, -s)), point(c + glm::vec2(-s * 0.2f, s)), line);
            draw->AddRectFilled(point(c + glm::vec2(s * 0.2f, -s)), point(c + glm::vec2(s * 0.7f, s)), line);
            break;
        case Icon::Start:
            for (const float y : {-0.6f, 0.0f, 0.6f}) {
                draw->AddLine(point(c + glm::vec2(-s, y * s)), point(c + glm::vec2(s, y * s)), line, thick);
            }
            break;
        case Icon::Voice: text("Jack"); break;
        case Icon::L1: text("L1"); break;
        case Icon::R1: text("R1"); break;
        }
    }
    for (const Finger& finger : fingers_) {
        if (finger.stick) {
            const float radius = kStickRadius * std::min(size_.x, size_.y);
            glm::vec2 offset = finger.position - finger.origin;
            if (glm::length(offset) > radius) {
                offset = glm::normalize(offset) * radius;
            }
            draw->AddCircle(point(finger.origin), radius, Colour(0.3f), 48, 3.0f);
            draw->AddCircleFilled(point(finger.origin + offset), radius * 0.42f, Colour(0.35f), 32);
        }
    }
}

}

#endif
