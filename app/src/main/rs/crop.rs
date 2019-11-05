#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.liberaid.renderscripttest)

uint32_t offsetX;
uint32_t offsetY;

rs_allocation croppedImg;

void RS_KERNEL crop(uchar4 in, uint32_t x, uint32_t y) {
    rsSetElementAt_uchar4(croppedImg, in, x - offsetX, y - offsetY);
}