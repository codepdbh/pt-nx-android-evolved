# Input system

State: gamepads use the engine's SDL3 path (compiled, untested on Android). Touch controls are not implemented, so the game cannot be played on touch alone.

Planned connection points in `InputDevice::Poll` (`src/engine/platform/input.cpp`): the virtual joystick into `left_stick`, camera drag into `right_stick`, buttons into `raw` bits (`kRawCross` and others), and a button for `voice_keyword_pressed`, which both triggers already set on gamepads.
