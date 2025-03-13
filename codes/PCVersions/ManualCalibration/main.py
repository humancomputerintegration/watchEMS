import pygame
import time
import serial
import atexit
import math

serialPort = "/dev/cu.usbmodem14201"

baudrate = 9600

ard = serial.Serial(serialPort, baudrate)
ard.flushInput()
msg = ard.read(ard.inWaiting())

pygame.init()

screen = pygame.display.set_mode((800, 600))

width = screen.get_width()
height = screen.get_height()
color = (255, 203, 59)

smallfont = pygame.font.SysFont('timesnewroman', 35)

pygame.display.set_caption("Manual Calibration App")
icon = pygame.image.load("img.jpeg")
pygame.display.set_icon(icon)
running = True


class Button:
    def __init__(self, x, y, wid, hei, text, text_distance, callback=None):
        self.x = x
        self.y = y
        self.tx = text_distance
        self.width = wid
        self.height = hei
        self.textVal = text

        smallfont = pygame.font.SysFont('timesnewroman', 35)
        self.text = smallfont.render(text, True, (0, 0, 0))
        self.callback = callback
        self.color_light = (255, 222, 130)
        self.color_dark = (252, 186, 3)

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
    def __init__(self, name, x, y, wid, hei, length, usingIncrements, increments=None, incInd=0, ranges=(0, 0),
                 rangeNum=0):
        self.name = name
        self.rangeNum = rangeNum
        self.incInd = incInd
        self.ranges = ranges
        self.width = wid
        self.height = hei
        self.length = length
        self.usingIncrements = usingIncrements
        self.increments = increments
        self.x = x
        self.y = y
        self.color_light = (255, 222, 130)
        self.color_dark = (252, 186, 3)
        self.slider_loc = x
        self.slider_value = float(self.slider_loc - self.x) / float(self.length)
        self.dragging = False

    def is_clicked(self, px, py):
        return self.slider_loc <= px <= self.slider_loc + self.width and self.y <= py <= self.y + self.height


intensitySlider = Slider("int", 165, 95, 40, 60, 300, True,
                         increments=["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", '15'])
channelSlider = Slider("ch", 165, 20, 40, 60, 300, True,
                       increments=["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"])
# pulseWidthSlider = Slider("pul", 165, 170, 40, 60, 300, False, ranges=(150, 500))
frequencySlider = Slider("fre", 165, 245, 40, 60, 300, True, increments=["4", "20", "100"])
frequencySlider.incInd = 2
frequencySlider.slider_loc = frequencySlider.x + (
        (frequencySlider.incInd / (len(frequencySlider.increments) - 1)) * frequencySlider.length)


def sendPulse():
    write_read("stim" + str(1000))


def startFunc():
    write_read("a")
    print("a")

    if startButton.textVal == "Start":
        startButton.text = smallfont.render("Stop", True, (0, 0, 0))
        startButton.textVal = "Stop"

    else:
        startButton.text = smallfont.render("Start", True, (0, 0, 0))
        startButton.textVal = "Start"


def setIndUp():
    write_read("indUpSet")


def setIndDown():
    write_read("indDownSet")


def getParams():
    write_read("param")
    time.sleep(1)
    write_read("fingerSettings")


pulseButton = Button(width / 2 - 270, height / 2 + 140, 140, 40, "Pulse", 20, callback=sendPulse)
startButton = Button(width / 2 - 270, height / 2 + 40, 140, 40, "Start", 20, callback=startFunc)

indexUp = Button(width / 2 + 70, height / 2 + 30, 200, 40, "Set IndExt", 20, callback=setIndUp)
indexDown = Button(width / 2 + 70, height / 2 + 80, 200, 40, "Set IndFlex", 20, callback=setIndDown)

getFingersAndParamsButton = Button(width / 2 + 70, height / 2 + 240, 200, 40, "Params", 20, callback=getParams)

buttons = [pulseButton, startButton, indexUp, indexDown, getFingersAndParamsButton]

slidersAll = [intensitySlider, channelSlider, frequencySlider]

def write_read(x, timeout=0.01):
    ard.write(bytes(x + "\n", 'utf-8'))

    print("Writing " + x)
    received_data = b''
    start_time = time.time()

    while True:
        if ard.in_waiting > 0:
            received_data += ard.read(ard.in_waiting)

        if time.time() - start_time > timeout:
            print(received_data.decode('utf-8'))
            break

    time.sleep(0.1)


write_read("setch1")

write_read("int1")

write_read("freq100")

write_read("pul400")


def close_serial():
    if ard.is_open:
        ard.close()
    print("Serial connection closed.")
    time.sleep(0.05)


atexit.register(close_serial)

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

            for (i, t) in enumerate(slidersAll):
                if t.is_clicked(mouse[0], mouse[1]):
                    t.dragging = True



        # mouse up
        elif ev.type == pygame.MOUSEBUTTONUP:

            for (i, t) in enumerate(slidersAll):
                if t.dragging:
                    if t.usingIncrements:
                        num = math.floor(t.slider_value * len(t.increments))
                        if num >= len(t.increments):
                            num -= 1
                        t.slider_loc = t.x + ((num / (len(t.increments) - 1)) * t.length)
                        t.dragging = False
                        t.incInd = num

                    else:
                        t.rangeNum = t.ranges[0] + round(t.slider_value * (t.ranges[1] - t.ranges[0]))
                        t.slider_loc = t.x + t.length * ((t.rangeNum - t.ranges[0]) / (t.ranges[1] - t.ranges[0]))
                        t.dragging = False

                    if t.name == "int":
                        write_read("int" + str(t.increments[t.incInd]))
                    elif t.name == "ch":
                        write_read("setch" + str(t.increments[t.incInd]))
                        write_read("int" + str(intensitySlider.increments[intensitySlider.incInd]))

                    elif t.name == "fre":
                        write_read("freq" + str(t.increments[t.incInd]))
                    elif t.name == "pul":
                        write_read("wid" + str(t.rangeNum))
        elif ev.type == pygame.KEYDOWN:
            if ev.key == pygame.K_UP:
                channelSlider.incInd += 1
                if channelSlider.incInd == 12:
                    channelSlider.incInd = 11
                channelSlider.slider_loc = channelSlider.x + (
                        (channelSlider.incInd / (len(channelSlider.increments) - 1)) * channelSlider.length)
                write_read("setch" + str(channelSlider.increments[channelSlider.incInd]))
                write_read("int" + str(intensitySlider.increments[intensitySlider.incInd]))


            elif ev.key == pygame.K_DOWN:
                channelSlider.incInd -= 1
                if channelSlider.incInd == -1:
                    channelSlider.incInd = 0
                channelSlider.slider_loc = channelSlider.x + (
                        (channelSlider.incInd / (len(channelSlider.increments) - 1)) * channelSlider.length)
                write_read("setch" + str(channelSlider.increments[channelSlider.incInd]))
                write_read("int" + str(intensitySlider.increments[intensitySlider.incInd]))

            elif ev.key == pygame.K_LEFT:
                intensitySlider.incInd -= 1
                if intensitySlider.incInd == -1:
                    intensitySlider.incInd = 0
                intensitySlider.slider_loc = intensitySlider.x + (
                        (intensitySlider.incInd / (len(intensitySlider.increments) - 1)) * intensitySlider.length)
                write_read("int" + str(intensitySlider.increments[intensitySlider.incInd]))
            elif ev.key == pygame.K_RIGHT:
                intensitySlider.incInd += 1
                if intensitySlider.incInd == 15:
                    intensitySlider.incInd = 14
                intensitySlider.slider_loc = intensitySlider.x + (
                        (intensitySlider.incInd / (len(intensitySlider.increments) - 1)) * intensitySlider.length)
                write_read("int" + str(intensitySlider.increments[intensitySlider.incInd]))

    mouse = pygame.mouse.get_pos()

    # dragging sliders
    for (i, t) in enumerate(slidersAll):
        if t.dragging:
            if (t.x + t.width / 2 <= mouse[0] <= t.x + t.length + t.width / 2) and t:
                t.slider_loc = mouse[0] - t.width / 2
                t.slider_value = float(t.slider_loc - t.x) / float(t.length)

    for (i, b) in enumerate(buttons):
        if b.is_clicked(mouse[0], mouse[1]):
            pygame.draw.rect(screen, b.color_light, [b.x, b.y, b.width, b.height])
        else:
            pygame.draw.rect(screen, b.color_dark, [b.x, b.y, b.width, b.height])
        screen.blit(b.text, (b.x + b.tx, b.y + b.height / 2 - 20))

    for (i, t) in enumerate(slidersAll):
        screen.blit(smallfont.render(str(t.name) + ": ", True, color),
                    (t.x - 60, t.y + t.height / 2 - 20))

        if (t.is_clicked(mouse[0], mouse[1])):

            pygame.draw.rect(screen, t.color_dark,
                             [t.x + t.width / 2, t.y + t.height / 2 - 3,
                              t.length, 6])

            pygame.draw.rect(screen, t.color_light,
                             [t.slider_loc, t.y, t.width, t.height])
        else:
            pygame.draw.rect(screen, t.color_dark,
                             [t.x + t.width / 2, t.y + t.height / 2 - 3,
                              t.length, 6])

            pygame.draw.rect(screen, t.color_dark,
                             [t.slider_loc, t.y, t.width, t.height])

        if t.usingIncrements:
            screen.blit(smallfont.render(t.increments[t.incInd], True, color),
                        (t.x + t.length + 80, t.y + t.height / 2 - 20))

        else:

            if (t.name == "int"):
                screen.blit(smallfont.render(str(t.rangeNum) + " mA", True, color),
                            (t.x + t.length + 80, t.y + t.height / 2 - 20))
            elif t.name == "fre":
                screen.blit(smallfont.render(str(t.rangeNum) + " Hz", True, color),
                            (t.x + t.length + 80, t.y + t.height / 2 - 20))
            elif t.name == "pul":
                screen.blit(smallfont.render(str(t.rangeNum) + " μs", True, color),
                            (t.x + t.length + 80, t.y + t.height / 2 - 20))

    pygame.display.flip()
