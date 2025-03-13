import pygame
import time
import serial
import atexit

serialPort = "/dev/cu.usbmodem14201"

baudrate = 9600

ard = serial.Serial(serialPort, baudrate)
ard.flushInput()
msg = ard.read(ard.inWaiting())

pygame.init()

screen = pygame.display.set_mode((800, 600))

width = screen.get_width()
height = screen.get_height()
color = (179, 66, 245)

smallfont = pygame.font.SysFont('timesnewroman', 35)

pygame.display.set_caption("Drumming App")
icon = pygame.image.load("ic_launcher-playstore.png")
pygame.display.set_icon(icon)
running = True


class Button:
    def __init__(self, x, y, wid, hei, text, text_distance, callback=None):
        self.x = x
        self.y = y
        self.tx = text_distance
        self.width = wid
        self.height = hei
        smallfont = pygame.font.SysFont('timesnewroman', 35)
        self.text = smallfont.render(text, True, color)
        self.callback = callback
        self.color_light = (226, 176, 255)
        self.color_dark = (98, 0, 156)

    def is_clicked(self, px, py):
        return self.x <= px <= self.x + self.width and self.y <= py <= self.y + self.height

    def click(self, param=None):
        if self.callback:
            if param is None:
                self.callback()
            else:
                self.callback(param)
        else:
            print(f"Button '{self.text}' clicked, but no action is set.")


class Slider:
    def __init__(self, x, y, wid, hei, length, increments=None):
        self.width = wid
        self.height = hei
        self.length = length
        self.increments = increments
        self.x = x
        self.y = y
        self.color_light = (226, 176, 255)
        self.color_dark = (98, 0, 156)
        self.slider_loc = x
        self.slider_value = float(self.slider_loc - self.x) / float(self.length)
        self.dragging = False

    def is_clicked(self, px, py):
        return self.slider_loc <= px <= self.slider_loc + self.width and self.y <= py <= self.y + self.height


class Toggle:
    def __init__(self, x, y, wid, hei, text, text_distance, callback=None):
        self.x = x
        self.y = y
        self.tx = text_distance
        self.width = wid
        self.height = hei
        self.direction = False
        smallfont = pygame.font.SysFont('timesnewroman', 35)
        self.text = smallfont.render(text, True, color)
        self.callback = callback
        self.color_light = (226, 176, 255)
        self.color_dark = (98, 0, 156)

    def is_clicked(self, px, py):
        return self.x <= px <= self.x + self.width and self.y <= py <= self.y + self.height

    def click(self, param=None):
        if self.callback:
            if param is None:
                self.callback()
            else:
                self.callback(param)
        else:
            print(f"Button '{self.text}' clicked, but no action is set.")


tempo_slider = Slider(165, 300, 40, 60, 300, ["30", "35", "40", "45", "50"])

tempo = 30

time_start = time.time()

playing = False

onBeat = 7

beatColOff = (58, 66, 79)
beatColOn = (153, 0, 255)

beatCols = [beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff]


def playBut():
    global playing, time_start, onBeat, beatCols
    if (playing):
        playing = False
        beatCols = [beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff, beatColOff]
        play.color_dark = (98, 0, 156)
        play.color_light = (226, 176, 255)

    else:
        playing = True
        onBeat = 7
        play.color_dark = (226, 176, 255)
        play.color_light = (244, 214, 255)

    time_start = time.time()


beats = [False, False, False, False, False, False, False, False]


def beat1Func(button):
    if beats[0]:
        beats[0] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[0] = True
        button.color_dark = (98, 0, 156)

    print("beat 1")


def beat2Func(button):
    if beats[1]:
        beats[1] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[1] = True
        button.color_dark = (98, 0, 156)

    print("beat 2")


def beat3Func(button):
    if beats[2]:
        beats[2] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[2] = True
        button.color_dark = (98, 0, 156)

    print("beat 3")


def beat4Func(button):
    if beats[3]:
        beats[3] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[3] = True
        button.color_dark = (98, 0, 156)

    print("beat 4")


def beat5Func(button):
    if beats[4]:
        beats[4] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[4] = True
        button.color_dark = (98, 0, 156)

    print("beat 5")


def beat6Func(button):
    if beats[5]:
        beats[5] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[5] = True
        button.color_dark = (98, 0, 156)

    print("beat 6")


def beat7Func(button):
    if beats[6]:
        beats[6] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[6] = True
        button.color_dark = (98, 0, 156)

    print("beat 7")


def beat8Func(button):
    if beats[7]:
        beats[7] = False
        button.color_dark = (97, 106, 120)
    else:
        beats[7] = True
        button.color_dark = (98, 0, 156)

    print("beat 8")


bothMode = True


def modeSelect():
    modeToggle.direction = not modeToggle.direction


play = Button(width / 2 - 70, height / 2 + 110, 140, 40, "Play", 40, callback=playBut)
quitButton = Button(width / 2 - 70, height / 2 + 170, 140, 40, "Quit", 40, callback=pygame.QUIT)

beat1 = Button(width / 2 - 235, height / 2 - 4 * 70, 50, 80, "1", 15, callback=beat1Func)
beat2 = Button(width / 2 - 175, height / 2 - 4 * 70, 50, 80, "2", 15, callback=beat2Func)
beat3 = Button(width / 2 - 115, height / 2 - 4 * 70, 50, 80, "3", 15, callback=beat3Func)
beat4 = Button(width / 2 - 55, height / 2 - 4 * 70, 50, 80, "4", 15, callback=beat4Func)
beat5 = Button(width / 2 + 5, height / 2 - 4 * 70, 50, 80, "5", 15, callback=beat5Func)
beat6 = Button(width / 2 + 65, height / 2 - 4 * 70, 50, 80, "6", 15, callback=beat6Func)
beat7 = Button(width / 2 + 125, height / 2 - 4 * 70, 50, 80, "7", 15, callback=beat7Func)
beat8 = Button(width / 2 + 185, height / 2 - 4 * 70, 50, 80, "8", 15, callback=beat8Func)

modeToggle = Toggle(width / 2 -100, height / 2 - 80, 50, 80, "1", 15, callback=modeSelect)

buttons = [play, quitButton]

beatButtons = [beat1, beat2, beat3, beat4, beat5, beat6, beat7, beat8]
for button in beatButtons:
    button.color_dark = (97, 106, 120)

tempo_text = smallfont.render("0 bpm", True, color)
screen.blit(tempo_text, (tempo_slider.x + tempo_slider.length + 80, tempo_slider.y + tempo_slider.height / 2 - 20))


def write_read(x, timeout=0.01):
    ard.write(bytes(x + "\n", 'utf-8'))
    print(x)
    received_data = b''
    start_time = time.time()


    while True:
        if ard.in_waiting > 0:
            received_data += ard.read(ard.in_waiting)

        if time.time() - start_time > timeout:
            print(received_data.decode('utf-8'))
            break


def close_serial():
    if ard.is_open:
        ard.close()
        print("Serial connection closed.")
        time.sleep(0.05)


atexit.register(close_serial)

indexIsDown = False
while running:
    screen.fill((0, 0, 0))

    # mouse events
    for ev in pygame.event.get():
        if ev.type == pygame.QUIT:
            pygame.quit()

        # buttons clicked
        if ev.type == pygame.MOUSEBUTTONDOWN:
            mouse = pygame.mouse.get_pos()
            print("x: " + str(mouse[0]) + " y: " + str(mouse[1]))
            for (i, b) in enumerate(buttons):
                if b.is_clicked(mouse[0], mouse[1]):
                    b.click()
            for (i, b) in enumerate(beatButtons):
                if b.is_clicked(mouse[0], mouse[1]):
                    b.click(b)

            if tempo_slider.is_clicked(mouse[0], mouse[1]):
                tempo_slider.dragging = True

            if modeToggle.is_clicked(mouse[0], mouse[1]):
                modeToggle.click()

        # mouse up
        elif ev.type == pygame.MOUSEBUTTONUP:
            if tempo_slider.dragging:
                num = round(tempo_slider.slider_value * 4)
                tempo_slider.slider_loc = (tempo_slider.length * float(num)) / 4.0 + tempo_slider.x
                tempo = int(tempo_slider.increments[num])
                tempo_slider.dragging = False
                print(tempo)

    mouse = pygame.mouse.get_pos()

    # dragging sliders

    if tempo_slider.dragging:
        if (mouse[0] >= tempo_slider.x + tempo_slider.width / 2 and mouse[
            0] <= tempo_slider.x + tempo_slider.length + tempo_slider.width / 2) and tempo_slider:
            tempo_slider.slider_loc = mouse[0] - tempo_slider.width / 2
            tempo_slider.slider_value = float(tempo_slider.slider_loc - tempo_slider.x) / float(tempo_slider.length)

    tempo_text = smallfont.render(str(tempo), True, color)
    screen.blit(tempo_text,
                (tempo_slider.x + tempo_slider.length + 80, tempo_slider.y + tempo_slider.height / 2 - 20))

    if playing:
        if modeToggle.direction:
            if time.time() - time_start >= 30.0 / tempo and not indexIsDown:
                indexIsDown = True
                beatCols[onBeat] = beatColOff
                onBeat = (onBeat + 1) % 8
                beatCols[onBeat] = beatColOn
                if beats[onBeat]:
                    # ard
                    write_read("indexDown")
            if time.time() - time_start >= 60.0 / tempo:
                indexIsDown = False
                time_start = time.time()
                if beats[onBeat]:
                    # ard
                    write_read("indexUp")
        else:
            if time.time() - time_start >= 60.0 / tempo:
                time_start = time.time()
                beatCols[onBeat] = beatColOff
                onBeat = (onBeat + 1) % 8
                beatCols[onBeat] = beatColOn
                if beats[onBeat]:
                    # ard
                    write_read("indexDown")

    for (i, b) in enumerate(buttons):
        if b.is_clicked(mouse[0], mouse[1]):
            pygame.draw.rect(screen, b.color_light, [b.x, b.y, b.width, b.height])
        else:
            pygame.draw.rect(screen, b.color_dark, [b.x, b.y, b.width, b.height])
        screen.blit(b.text, (b.x + b.tx, b.y + b.height / 2 - 20))

    for (i, b) in enumerate(beatButtons):
        if b.is_clicked(mouse[0], mouse[1]):
            pygame.draw.rect(screen, b.color_light, [b.x, b.y, b.width, b.height])
        else:
            pygame.draw.rect(screen, b.color_dark, [b.x, b.y, b.width, b.height])
        pygame.draw.circle(screen, beatCols[i], (b.x + b.width / 2, b.y + b.height + 30), 10)
        screen.blit(b.text, (b.x + b.tx, b.y + b.height / 2 - 20))

    if tempo_slider.is_clicked(mouse[0], mouse[1]):
        pygame.draw.rect(screen, tempo_slider.color_dark,
                         [tempo_slider.x + tempo_slider.width / 2, tempo_slider.y + tempo_slider.height / 2 - 3,
                          tempo_slider.length, 6])

        pygame.draw.rect(screen, tempo_slider.color_light,
                         [tempo_slider.slider_loc, tempo_slider.y, tempo_slider.width, tempo_slider.height])

    else:
        pygame.draw.rect(screen, tempo_slider.color_dark,
                         [tempo_slider.x + tempo_slider.width / 2, tempo_slider.y + tempo_slider.height / 2 - 3,
                          tempo_slider.length, 6])

        pygame.draw.rect(screen, tempo_slider.color_dark,
                         [tempo_slider.slider_loc, tempo_slider.y, tempo_slider.width, tempo_slider.height])

    if True:
        pygame.draw.rect(screen, (128, 128, 128),
                         [modeToggle.x, modeToggle.y + modeToggle.height/2 - 5, modeToggle.width, 10])
        smallfont = pygame.font.SysFont('timesnewroman', 35)
        if modeToggle.is_clicked(mouse[0], mouse[1]):
            if modeToggle.direction:

                pygame.draw.rect(screen, modeToggle.color_light,
                                 [modeToggle.x + modeToggle.width/2, modeToggle.y, modeToggle.width/2, modeToggle.height])
                modeToggle.text = smallfont.render("Complex Mode", True, color)

            else:
                pygame.draw.rect(screen, modeToggle.color_light,
                                 [modeToggle.x, modeToggle.y, modeToggle.width/2, modeToggle.height])
                modeToggle.text = smallfont.render("Normal  Mode", True, color)

        else:
            if modeToggle.direction:

                pygame.draw.rect(screen, modeToggle.color_dark,
                                 [modeToggle.x + modeToggle.width / 2, modeToggle.y, modeToggle.width / 2,
                                  modeToggle.height])
                modeToggle.text = smallfont.render("Complex Mode", True, color)

            else:
                pygame.draw.rect(screen, modeToggle.color_dark,
                                 [modeToggle.x, modeToggle.y, modeToggle.width / 2, modeToggle.height])
                modeToggle.text = smallfont.render("Normal  Mode", True, color)

        screen.blit(modeToggle.text, (modeToggle.x + modeToggle.width+20, modeToggle.y + modeToggle.height / 2 - 20))

    pygame.display.flip()
