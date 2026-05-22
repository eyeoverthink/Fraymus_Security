/* ═══════════════════════════════════════════════════════════════════════════
 * FRAY PALETTE DATA - Gen 144
 * 256-color palette for Mode 13h
 * ═══════════════════════════════════════════════════════════════════════════ */

#ifndef FRAY_PALETTE_C
#define FRAY_PALETTE_C

/* Golden ratio color scheme */
static unsigned char phi_palette[16][3] = {
    {0,   0,   0},     /* 0: Black */
    {26,  26,  42},    /* 1: Deep Blue */
    {42,  68,  68},    /* 2: Teal */
    {68,  110, 89},    /* 3: Forest */
    {110, 137, 89},    /* 4: Olive */
    {137, 137, 68},    /* 5: Gold */
    {178, 144, 68},    /* 6: Amber */
    {200, 178, 110},   /* 7: Sand */
    {222, 200, 144},   /* 8: Cream */
    {233, 222, 178},   /* 9: Ivory */
    {255, 233, 200},   /* 10: Pearl */
    {255, 245, 222},   /* 11: Snow */
    {200, 89,  68},    /* 12: Rust */
    {178, 68,  89},    /* 13: Wine */
    {144, 89,  137},   /* 14: Purple */
    {255, 255, 255}    /* 15: White */
};

void vga_load_phi_palette(void) {
    for (int i = 0; i < 16; i++) {
        vga_set_palette_color(i,
            phi_palette[i][0],
            phi_palette[i][1],
            phi_palette[i][2]);
    }
}

#endif /* FRAY_PALETTE_C */
