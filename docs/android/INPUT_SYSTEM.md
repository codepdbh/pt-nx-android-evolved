# Input system

`src/engine/platform/touch_controls.cpp` is compiled on Android only. `InputDevice::Poll` treats it as one more pad, so the game logic has no touch code. A connected gamepad hides the controls.

- In play: floating stick on the left half, camera drag on the right half, buttons for cross (interact), R3 (zoom), Start (pause), and "Jack" only while the game listens for the word.
- In menus: virtual d-pad, the four face buttons, L1, R1 and Start. A tap elsewhere stays the mouse click SDL synthesizes, which the menus already handle.
- Prompts show PlayStation shapes while the on-screen controls are active.
- The system Back button opens the pause menu.

Tested on the S25 Ultra: the stick moves the player and the layout fits the screen. Not tested: every menu button, the Jack button in its event, gamepads.

Pending: size, opacity, position and sensitivity settings.
