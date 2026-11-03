# Texture compatibility

`ftex.cpp` maps game textures to BC1, BC2, BC3, BC5 and BC7, among others. They are uploaded unchanged, so `textureCompressionBC` is required.

Adreno 830 reports the feature, so no conversion is needed on the test device. Per-format support and real uploads have not been verified.

Pending for devices without BC: decode or transcode (ASTC or ETC2 where suitable, keeping sRGB, normal maps, alpha and mipmaps correct), with a persistent cache. Not implemented.
