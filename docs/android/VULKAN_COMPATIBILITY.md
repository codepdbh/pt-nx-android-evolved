# Vulkan compatibility

`Context::Init` checks these with `vkGetPhysicalDeviceFeatures2` and refuses the device, naming what is missing. There are no fallback paths yet.

Required: Vulkan 1.3, dynamicRendering, synchronization2, shaderDemoteToHelperInvocation, descriptorIndexing, runtimeDescriptorArray, shaderSampledImageArrayNonUniformIndexing, descriptorBindingPartiallyBound, descriptorBindingVariableDescriptorCount, descriptorBindingSampledImageUpdateAfterBind, descriptorBindingUpdateUnusedWhilePending, descriptorBindingStorageBufferUpdateAfterBind, timelineSemaphore, scalarBlockLayout, samplerAnisotropy, textureCompressionBC, fillModeNonSolid, shaderInt16, shaderClipDistance.

## Measured: Adreno 830 (`adb shell cmd gpu vkjson`)

Vulkan 1.3.284, driver build 0603ba43d2 (06/22/26). All required features present. Ray query and acceleration structure extensions are listed, but ray queries have not been used. `VK_EXT_memory_budget` is absent, so the engine has no budget figure. Subgroups: size 64, fragment and compute stages, operations 0x2BF.

## Swapchain on Android

- Identity `preTransform`: the renderer draws upright and the compositor rotates, at the cost of an extra pass.
- Android then returns `VK_SUBOPTIMAL_KHR` on every present. `vk::SwapchainStale` ignores it there; otherwise the swapchain is rebuilt every frame, as first observed.
- Composite alpha falls back to inherit when opaque is not offered.

## Shaders

73 shaders compile with the NDK's `glslc` for Vulkan 1.3 and are packed into the APK. The self test starts the scene renderer on them; pipelines used only with game data remain unverified.
