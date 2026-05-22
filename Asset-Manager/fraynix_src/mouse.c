/* FRAY-MOUSE: PS/2 MOUSE DRIVER - Gen 146 */

#ifndef FRAY_MOUSE_C
#define FRAY_MOUSE_C

#define MOUSE_PORT   0x60
#define MOUSE_STATUS 0x64
#define MOUSE_CMD    0x64

static int mouse_x = 160;
static int mouse_y = 100;
static int mouse_buttons = 0;
static int mouse_was_pressed = 0;

static unsigned char mouse_cycle = 0;
static signed char mouse_bytes[3];

void mouse_wait_write(void) {
    int timeout = 100000;
    while (timeout-- && (inb(MOUSE_STATUS) & 2));
}

void mouse_wait_read(void) {
    int timeout = 100000;
    while (timeout-- && !(inb(MOUSE_STATUS) & 1));
}

void mouse_write(unsigned char val) {
    mouse_wait_write();
    outb(MOUSE_CMD, 0xD4);
    mouse_wait_write();
    outb(MOUSE_PORT, val);
}

unsigned char mouse_read(void) {
    mouse_wait_read();
    return inb(MOUSE_PORT);
}

void mouse_init(void) {
    /* Enable auxiliary device */
    mouse_wait_write();
    outb(MOUSE_CMD, 0xA8);

    /* Enable interrupts */
    mouse_wait_write();
    outb(MOUSE_CMD, 0x20);
    mouse_wait_read();
    unsigned char status = inb(MOUSE_PORT) | 2;
    mouse_wait_write();
    outb(MOUSE_CMD, 0x60);
    mouse_wait_write();
    outb(MOUSE_PORT, status);

    /* Use default settings */
    mouse_write(0xF6);
    mouse_read();

    /* Enable mouse */
    mouse_write(0xF4);
    mouse_read();
}

void mouse_handle_packet(void) {
    unsigned char status = inb(MOUSE_STATUS);
    if (!(status & 1)) return;

    signed char val = inb(MOUSE_PORT);

    mouse_bytes[mouse_cycle] = val;
    mouse_cycle++;

    if (mouse_cycle == 3) {
        mouse_cycle = 0;

        /* Parse packet */
        mouse_buttons = mouse_bytes[0] & 0x07;

        int dx = mouse_bytes[1];
        int dy = mouse_bytes[2];

        /* Sign extend */
        if (mouse_bytes[0] & 0x10) dx |= 0xFFFFFF00;
        if (mouse_bytes[0] & 0x20) dy |= 0xFFFFFF00;

        mouse_x += dx;
        mouse_y -= dy;

        /* Clamp */
        if (mouse_x < 0) mouse_x = 0;
        if (mouse_x > 319) mouse_x = 319;
        if (mouse_y < 0) mouse_y = 0;
        if (mouse_y > 199) mouse_y = 199;
    }
}

void mouse_update(void) {
    while (inb(MOUSE_STATUS) & 1) {
        mouse_handle_packet();
    }
}

#endif /* FRAY_MOUSE_C */
