/* FRAY-WIDGETS: UI COMPONENTS - Gen 146 */

#ifndef FRAY_WIDGETS_C
#define FRAY_WIDGETS_C

typedef struct {
    int x, y, w, h;
    char label[16];
    unsigned char bg_color;
    unsigned char text_color;
    int pressed;
    void (*on_click)(void);
} Button;

typedef struct {
    int x, y, w, h;
    char text[256];
    int cursor_pos;
} TextBox;

void widget_draw_button(Button* btn) {
    unsigned char color = btn->pressed ? btn->bg_color - 2 : btn->bg_color;
    vga_fill_rect(btn->x, btn->y, btn->w, btn->h, color);
    vga_rect(btn->x, btn->y, btn->w, btn->h, 15);

    if (btn->pressed) {
        vga_hline(btn->x, btn->x + btn->w - 1, btn->y, 8);
        vga_vline(btn->x, btn->y, btn->y + btn->h - 1, 8);
    } else {
        vga_hline(btn->x, btn->x + btn->w - 1, btn->y + btn->h - 1, 8);
        vga_vline(btn->x + btn->w - 1, btn->y, btn->y + btn->h - 1, 8);
    }
}

int widget_button_hit(Button* btn, int mx, int my) {
    return (mx >= btn->x && mx < btn->x + btn->w &&
            my >= btn->y && my < btn->y + btn->h);
}

void widget_draw_textbox(TextBox* tb) {
    vga_fill_rect(tb->x, tb->y, tb->w, tb->h, 15);
    vga_rect(tb->x, tb->y, tb->w, tb->h, 0);
}

void widget_draw_progress(int x, int y, int w, int h, int value, int max, unsigned char color) {
    vga_fill_rect(x, y, w, h, 8);
    int fill_w = (w * value) / max;
    vga_fill_rect(x, y, fill_w, h, color);
    vga_rect(x, y, w, h, 0);
}

void widget_draw_scrollbar(int x, int y, int h, int pos, int max) {
    vga_fill_rect(x, y, 12, h, 7);
    int thumb_h = h / 4;
    int thumb_y = y + (pos * (h - thumb_h)) / max;
    vga_fill_rect(x + 1, thumb_y, 10, thumb_h, 15);
    vga_rect(x, y, 12, h, 0);
}

#endif /* FRAY_WIDGETS_C */
