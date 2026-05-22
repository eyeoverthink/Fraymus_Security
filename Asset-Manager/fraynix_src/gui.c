/* FRAY-GUI: DESKTOP ENVIRONMENT - Gen 146 */

#ifndef FRAY_GUI_C
#define FRAY_GUI_C

#include "mouse.c"
#include "widgets.c"

/* Theme Colors */
#define COLOR_BG        1    /* Deep Blue */
#define COLOR_TASKBAR   23   /* Silver */
#define COLOR_WINDOW    24   /* Dark Gray */
#define COLOR_TITLE     14   /* Gold */
#define COLOR_SHADOW    0    /* Black */
#define COLOR_TEXT      15   /* White */
#define COLOR_BUTTON    8    /* Gray */
#define COLOR_CLOSE     4    /* Red */

/* Desktop state */
static int desktop_running = 1;
static int active_window = -1;

/* Icons */
typedef struct {
    int x, y;
    char name[16];
    unsigned char color;
    void (*action)(void);
} DesktopIcon;

static DesktopIcon icons[8];
static int icon_count = 0;

/* Windows */
typedef struct {
    int x, y, w, h;
    char title[32];
    int visible;
    int dragging;
    int drag_x, drag_y;
} Window;

#define MAX_WINDOWS 8
static Window windows[MAX_WINDOWS];
static int window_count = 0;

/* Forward declarations */
void doom_loop(void);
void cmd_info(void);
void compiler_help(void);

void desktop_add_icon(int x, int y, const char* name, unsigned char color, void (*action)(void)) {
    if (icon_count >= 8) return;
    icons[icon_count].x = x;
    icons[icon_count].y = y;
    for (int i = 0; name[i] && i < 15; i++) icons[icon_count].name[i] = name[i];
    icons[icon_count].color = color;
    icons[icon_count].action = action;
    icon_count++;
}

int desktop_create_window(int x, int y, int w, int h, const char* title) {
    if (window_count >= MAX_WINDOWS) return -1;
    windows[window_count].x = x;
    windows[window_count].y = y;
    windows[window_count].w = w;
    windows[window_count].h = h;
    for (int i = 0; title[i] && i < 31; i++) windows[window_count].title[i] = title[i];
    windows[window_count].visible = 1;
    windows[window_count].dragging = 0;
    return window_count++;
}

void desktop_draw_icon(DesktopIcon* icon) {
    vga_fill_rect(icon->x, icon->y, 32, 32, icon->color);
    vga_rect(icon->x, icon->y, 32, 32, 15);
}

void desktop_draw_window(Window* win) {
    if (!win->visible) return;

    /* Shadow */
    vga_fill_rect(win->x + 3, win->y + 3, win->w, win->h, COLOR_SHADOW);

    /* Window body */
    vga_fill_rect(win->x, win->y, win->w, win->h, COLOR_WINDOW);

    /* Title bar */
    vga_fill_rect(win->x, win->y, win->w, 14, COLOR_TITLE);

    /* Close button */
    vga_fill_rect(win->x + win->w - 12, win->y + 2, 10, 10, COLOR_CLOSE);

    /* Border */
    vga_rect(win->x, win->y, win->w, win->h, 15);
}

void desktop_draw_taskbar(void) {
    vga_fill_rect(0, 185, 320, 15, COLOR_TASKBAR);
    vga_fill_rect(2, 187, 40, 11, COLOR_TITLE);  /* Start button */
    vga_rect(2, 187, 40, 11, 0);
}

void desktop_draw_cursor(void) {
    int mx = mouse_x;
    int my = mouse_y;

    vga_put_pixel(mx, my, 15);
    vga_put_pixel(mx+1, my, 15);
    vga_put_pixel(mx, my+1, 15);
    vga_put_pixel(mx+1, my+1, 0);
    vga_put_pixel(mx+2, my+2, 15);
    vga_put_pixel(mx+3, my+3, 15);
}

int desktop_check_icon_click(int mx, int my) {
    for (int i = 0; i < icon_count; i++) {
        if (mx >= icons[i].x && mx < icons[i].x + 32 &&
            my >= icons[i].y && my < icons[i].y + 32) {
            return i;
        }
    }
    return -1;
}

void desktop_handle_click(int mx, int my, int button) {
    if (button != 1) return;

    /* Check icons */
    int icon = desktop_check_icon_click(mx, my);
    if (icon >= 0 && icons[icon].action) {
        icons[icon].action();
        return;
    }

    /* Check window title bars for drag */
    for (int i = window_count - 1; i >= 0; i--) {
        Window* w = &windows[i];
        if (!w->visible) continue;

        if (mx >= w->x && mx < w->x + w->w &&
            my >= w->y && my < w->y + 14) {

            /* Close button? */
            if (mx >= w->x + w->w - 12) {
                w->visible = 0;
                return;
            }

            /* Start drag */
            w->dragging = 1;
            w->drag_x = mx - w->x;
            w->drag_y = my - w->y;
            active_window = i;
            return;
        }
    }
}

void desktop_handle_drag(int mx, int my) {
    for (int i = 0; i < window_count; i++) {
        Window* w = &windows[i];
        if (w->dragging) {
            w->x = mx - w->drag_x;
            w->y = my - w->drag_y;
            if (w->x < 0) w->x = 0;
            if (w->y < 0) w->y = 0;
            if (w->x + w->w > 320) w->x = 320 - w->w;
            if (w->y + w->h > 185) w->y = 185 - w->h;
        }
    }
}

void desktop_handle_release(void) {
    for (int i = 0; i < window_count; i++) {
        windows[i].dragging = 0;
    }
}

void start_desktop(void) {
    vga_init();
    vga_init_palette();
    mouse_init();

    /* Add desktop icons */
    desktop_add_icon(20, 20, "DOOM", 4, doom_loop);
    desktop_add_icon(20, 60, "INFO", 2, cmd_info);
    desktop_add_icon(20, 100, "CODE", 1, compiler_help);

    /* Create default window */
    desktop_create_window(100, 40, 150, 100, "Terminal");

    while (desktop_running) {
        /* Background */
        vga_clear(COLOR_BG);

        /* Icons */
        for (int i = 0; i < icon_count; i++) {
            desktop_draw_icon(&icons[i]);
        }

        /* Windows */
        for (int i = 0; i < window_count; i++) {
            desktop_draw_window(&windows[i]);
        }

        /* Taskbar */
        desktop_draw_taskbar();

        /* Mouse */
        mouse_update();

        if (mouse_buttons & 1) {
            if (!mouse_was_pressed) {
                desktop_handle_click(mouse_x, mouse_y, 1);
                mouse_was_pressed = 1;
            } else {
                desktop_handle_drag(mouse_x, mouse_y);
            }
        } else {
            if (mouse_was_pressed) {
                desktop_handle_release();
                mouse_was_pressed = 0;
            }
        }

        desktop_draw_cursor();

        vga_wait_vsync();
    }
}

#endif /* FRAY_GUI_C */
