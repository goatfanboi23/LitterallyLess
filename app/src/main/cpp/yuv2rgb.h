// yuv2rgb.h

#include <cstdint>

namespace MyProject {

// Converts the YUV image with the given properties to ARGB image and write to
// `argb_output` destination.
    void Yuv2Rgb(int width, int height, const uint8_t* y_buffer, const uint8_t* u_buffer,
                 const uint8_t* v_buffer, int y_pixel_stride, int uv_pixel_stride,
                 int y_row_stride, int uv_row_stride, int* argb_output);

}