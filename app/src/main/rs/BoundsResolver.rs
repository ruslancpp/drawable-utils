#pragma version(1)
#pragma rs java_package_name(com.infiniteset.drawableutils)

/*
    Bounds of nontransparent area.
    An array of 4 items:
        data[0] is a left bound
        data[1] is a top bound
        data[2] is a right bound
        data[3] is a bottom bound
*/
int * bounds;

/*
    Resolves bounds of nontransparent area of image. Result of this procedure execution is written into *data.
*/
void resolveBounds(const uchar4 *v_in, uint32_t x, uint32_t y) {
    if (v_in->a == 0) return;

    rsAtomicMin(&bounds[0], x);
    rsAtomicMin(&bounds[1], y);
    rsAtomicMax(&bounds[2], x);
    rsAtomicMax(&bounds[3], y);
}