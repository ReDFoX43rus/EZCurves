#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.liberaid.renderscripttest)

uint8_t *mapping_r;
uint8_t *mapping_g;
uint8_t *mapping_b;

uchar4 RS_KERNEL apply(uchar4 in) {
    uchar4 out = in;
    out.r = mapping_r[out.r];
    out.g = mapping_g[out.g];
    out.b = mapping_b[out.b];

    return out;
}