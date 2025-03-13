import pygame
import time
import serial
import atexit

serialPort = "/dev/cu.usbmodem14101"

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

pygame.display.set_caption("Calibration App")
icon = pygame.image.load("ic_launcher-playstore.png")
pygame.display.set_icon(icon)
running = True

fingers = []

buttonMakeVisible = []

displayCalibText = False

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
        self.visible = False

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


calibrating = False


def startFunc():
    global thumbButton,indexButton,wristButton,middleButton, current_channel,current_intensity
    startButton.visible = False
    print("Starting calibration")
    indexButton.visible = True
    channels = [-1,-1,-1,-1]
    intensities = [-1,-1,-1,-1]
    fingers=[]
    current_channel = 1
    current_intensity = 0
    thumbButton.visible = True
    # wristButton.visible = True
    # middleButton.visible = True
    doneButton.visible = True


channels = [-1, -1, -1, -1]
intensities = [-1, -1, -1, -1]

current_channel = 1
current_intensity = 1


def doneSelecting():
    global displayCalibText
    indexButton.visible = False
    thumbButton.visible = False
    # wristButton.visible = False
    # middleButton.visible = False
    thumbButton.color_dark = (98, 0, 156)
    # middleButton.color_dark = (98, 0, 156)
    # wristButton.color_dark = (98, 0, 156)
    indexButton.color_dark = (98, 0, 156)
    displayCalibText = True
    yesButton.visible = True
    moreButton.visible = True
    nextButton.visible = True


def indexFunc():
    global calibrating
    if calibrating:
        channels[1] = current_channel
        intensities[1] = current_intensity
        doneSelecting()
    else:
        added = False
        for finger in fingers:
            if finger == "Index":
                added = True

        if added:
            fingers.remove("Index")
            indexButton.color_dark = (98, 0, 156)
        else:
            fingers.append("Index")
            indexButton.color_dark = (226, 176, 255)



def thumbFunc():
    global calibrating
    if calibrating:
        channels[0] = current_channel
        intensities[0] = current_intensity
        doneSelecting()
    else:
        added = False
        for finger in fingers:
            if finger == "Thumb":
                added = True

        if added:
            fingers.remove("Thumb")
            thumbButton.color_dark = (98, 0, 156)
        else:
            fingers.append("Thumb")
            thumbButton.color_dark = (226, 176, 255)


def middleFunc():
    global calibrating
    if calibrating:
        channels[2] = current_channel
        intensities[2] = current_intensity
        doneSelecting()
    else:
        added = False
        for finger in fingers:
            if finger == "Middle":
                added = True

        if added:
            fingers.remove("Middle")
            middleButton.color_dark = (98, 0, 156)
        else:
            fingers.append("Middle")
            middleButton.color_dark = (226, 176, 255)


def wristFunc():
    global calibrating
    if calibrating:
        channels[3] = current_channel
        intensities[3] = current_intensity
        doneSelecting()
    else:
        added = False
        for finger in fingers:
            if finger == "Wrist":
                added = True

        if added:
            fingers.remove("Wrist")
            wristButton.color_dark = (98, 0, 156)
        else:
            fingers.append("Wrist")
            wristButton.color_dark = (226, 176, 255)


def fingersSelected():
    global calibrating, displayCalibText
    indexButton.visible = False
    # middleButton.visible = False
    thumbButton.visible = False
    # wristButton.visible = False
    doneButton.visible = False
    yesButton.visible = True
    moreButton.visible = True
    nextButton.visible = True
    calibrating = True
    displayCalibText = True
    write_read("int"+str(current_channel))
    time.sleep(0.1)
    write_read("setch"+str(current_channel))


def moreStim():
    global current_intensity, current_channel
    current_intensity += 1
    write_read("int" + str(current_intensity))
    time.sleep(0.2)
    write_read("stim1000")


def nextChannel():
    global current_channel, current_intensity,displayCalibText
    current_channel += 1
    if current_channel > 12:
        done = True
        displayCalibText = False
        moreButton.visible = False
        yesButton.visible = False
        nextButton.visible = False
        for (index, fing) in enumerate(fingers):
            if fing == "Thumb":
                if channels[0] == -1:
                    done = False
            if fing == "Index":
                if channels[1] == -1:
                    done = False
            if fing == "Middle":
                if channels[2] == -1:
                    done = False
            if fing == "Wrist":
                if channels[3] == -1:
                    done = False
        if done:
            for (index, fing) in enumerate(fingers):
                if fing == "Thumb":
                    write_read("indDownSet"+str(channels[0])+","+str(intensities[0]))
                if fing == "Index":
                    write_read("indUpSet"+str(channels[1])+","+str(intensities[1]))
                if fing == "Middle":
                    write_read("middleSet"+str(channels[2])+","+str(intensities[2]))
                if fing == "Wrist":
                    write_read("wristSet"+str(channels[3])+","+str(intensities[3]))
                time.sleep(0.1)
            doneOtherButton.visible = True
        else:
            doneOtherButton.visible = True
            doneButton.text = "Retry"
        current_intensity = 1
    else:
        write_read("setch"+str(current_channel))
        time.sleep(0.1)
        current_intensity = 1
        write_read("int"+str(1))

        time.sleep(0.1)
        write_read("stim1000")





def whichFinger():
    global displayCalibText
    displayCalibText = False
    moreButton.visible = False
    yesButton.visible = False
    nextButton.visible = False

    for (index, fing) in enumerate(fingers):
        if fing == "Thumb":
            thumbButton.visible = True
            thumbButton.color_dark = (98, 0, 156)

        elif fing == "Index":
            indexButton.visible = True
            indexButton.color_dark = (98, 0, 156)

        elif fing == "Middle":
            middleButton.visible = True
            middleButton.color_dark = (98, 0, 156)

        elif fing == "Wrist":
            wristButton.visible = True
            wristButton.color_dark = (98, 0, 156)


def done():
    global calibrating
    doneOtherButton.visible = False
    startButton.visible = True
    calibrating = False


def write_read(x, timeout=0.01):
    ard.write(bytes(x + "\n", 'utf-8'))

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


startButton = Button(width / 2 - 70, height / 2, 140, 40, "Start", 20, callback=startFunc)
startButton.visible = True
indexButton = Button(width / 2 - 70, height / 2 - 250, 140, 40, "Ext", 20, callback=indexFunc)
thumbButton = Button(width / 2 - 70, height / 2 - 200, 140, 40, "Flex", 20, callback=thumbFunc)
# middleButton = Button(width / 2 - 70, height / 2 - 150, 140, 40, "Middle", 20, callback=middleFunc)
# wristButton = Button(width / 2 - 70, height / 2 - 100, 140, 40, "Wrist", 20, callback=wristFunc)

doneButton = Button(width / 2 - 70, height / 2, 140, 40, "Done", 20, callback=fingersSelected)

moreButton = Button(width / 2 - 70, height / 2 + 50, 140, 40, "More", 20, callback=moreStim)
yesButton = Button(width / 2 - 70, height / 2 - 50, 140, 40, "Yes", 20, callback=whichFinger)
nextButton = Button(width / 2 - 70, height / 2 + 150, 140, 40, "Next", 20, callback=nextChannel)
doneOtherButton = Button(width / 2 - 70, height / 2 + 50, 140, 40, "Done", 20, callback=done)

# buttons = [doneOtherButton,doneButton, moreButton, yesButton,
#            nextButton, indexButton, middleButton, thumbButton, wristButton,  startButton]

buttons = [doneOtherButton,doneButton, moreButton, yesButton,
           nextButton, thumbButton, indexButton,  startButton]

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
                if b.visible:
                    if b.is_clicked(mouse[0], mouse[1]):
                        b.click()

    mouse = pygame.mouse.get_pos()

    for button in buttonMakeVisible:
        button.visible = False
    buttonMakeVisible = []
    if displayCalibText:
        textInstruct = smallfont.render("Did your finger/hand move?", True, color)
        screen.blit(textInstruct, (width/4, height / 4 ))
    for (i, b) in enumerate(buttons):
        if b.visible:
            if b.is_clicked(mouse[0], mouse[1]):
                pygame.draw.rect(screen, b.color_light, [b.x, b.y, b.width, b.height])
            else:
                pygame.draw.rect(screen, b.color_dark, [b.x, b.y, b.width, b.height])
            screen.blit(b.text, (b.x + b.tx, b.y + b.height / 2 - 20))

    pygame.display.update()
