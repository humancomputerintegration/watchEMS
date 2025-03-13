import socket
import threading
import queue
import time
import pygame
import atexit
import math



HEADERSIZE = 10


def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
    except Exception:
        local_ip = "127.0.0.1"
    finally:
        s.close()
    return local_ip


server_ip = get_local_ip()

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((server_ip, 1234))
s.listen(5)
print(f"Server is starting on IP: {server_ip}")


# def send_messages(client_socket, send_queue):
#     retry = False
#     message = ""
#     while True:
#         # Check if there are messages to send
#
#         try:
#             if retry:
#                 message_with_header = f'{len(message):<{HEADERSIZE}}' + message
#
#                 if client_socket.fileno() != -1:  # Check if the socket is open
#                     client_socket.send(bytes(message_with_header + "\n", "utf-8"))
#                     retry = False
#                     print(f"Sent message: {message}")
#             else:
#
#                 message = input("")  # Wait for a message or timeout after 1 second
#                 if message is None:  # Handle termination
#                     break
#
#                 # Prepare message with header
#                 message_with_header = f'{len(message):<{HEADERSIZE}}' + message
#
#                 if client_socket.fileno() != -1:  # Check if the socket is open
#                     client_socket.send(bytes(message_with_header + "\n", "utf-8"))
#                     retry = False
#                     print(f"Sent message: {message}")
#
#                 else:
#                     retry = True
#                     print("Socket is closed, cannot send message.")
#
#
#         except queue.Empty:
#             continue
def send_messages(client_socket, messageSend):
    retry = False
    message = messageSend
    totalTries = 10
    while not retry and totalTries > 0:
        # Check if there are messages to send
        totalTries -= 1
        try:
            if message is None:  # Handle termination
                break

            # Prepare message with header
            message_with_header = f'{len(message):<{HEADERSIZE}}' + message

            if client_socket.fileno() != -1:  # Check if the socket is open
                client_socket.send(bytes(message_with_header + "\n", "utf-8"))
                retry = True
                print(f"Sent message: {message}")

            else:
                if totalTries==0:
                    print("Socket is closed, will retry.")


        except queue.Empty:
            continue


def handle_client(client_socket):
    send_queue = queue.Queue()  # Queue for outgoing messages
    # thread = threading.Thread(target=send_messages, args=(client_socket, send_queue))
    # thread.start()

    while True:
        try:
            # Receive the header
            header = client_socket.recv(HEADERSIZE)
            if not header:
                print("Connection closed by the client.")
                break

            # Convert header to integer message length
            msglen = int(header.decode("utf-8").strip())
            fullmsg = client_socket.recv(msglen).decode("utf-8")
            print(f"Received message: {fullmsg}")

            # Echo back to the client (you can also push messages to the send_queue here if needed)
            send_queue.put("Message received!")  # Add a message to send back
        except Exception as e:
            print(f"Error: {e}")
            break
    client_socket.close()



pygame.init()

screen = pygame.display.set_mode((800, 600))

width = screen.get_width()
height = screen.get_height()
color = (255, 203, 59)

smallfont = pygame.font.SysFont('timesnewroman', 35)

pygame.display.set_caption("IPS DEMO")
icon = pygame.image.load("img.jpg")
pygame.display.set_icon(icon)
running = True
def closeAll():
    s.close()


atexit.register(closeAll)

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


def rightCall(clientsocket):
    send_messages(clientsocket, "indexDown")

def leftCall(clientsocket):
    send_messages(clientsocket, "indexUp")


def thumbCall(clientsocket):
    send_messages(clientsocket, "shake1")
    time.sleep(0.4)
    send_messages(clientsocket, "shake2")
    time.sleep(0.4)
    send_messages(clientsocket, "shake1")
    time.sleep(0.4)
    send_messages(clientsocket, "shake2")





right = Button(width / 2 - 140, height / 2 -125, 280, 100, "Right", 20, callback=rightCall)
left = Button(width /2-140 , height /2 +25, 280, 100, "Left", 20, callback=leftCall)
thumb = Button(width / 2 - 140, height / 2 +150, 280, 100, "Thumb", 20, callback=thumbCall)
buttons = [right, left, thumb]

startedConnect = False
while not startedConnect:
    clientsocket, address = s.accept()
    print(f"Connection from: {address} has been established!")
    startedConnect = True
    thread = threading.Thread(target=handle_client, args=(clientsocket,))
    thread.start()

while True:


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
                    b.click(param = clientsocket)


        elif ev.type == pygame.KEYDOWN:
            if ev.key == pygame.K_LEFT:
                left.click(param=clientsocket)

            elif ev.key == pygame.K_RIGHT:
                right.click(param=clientsocket)

            elif ev.key == pygame.K_UP:
                thumb.click(param=clientsocket)

    mouse = pygame.mouse.get_pos()


    for (i, b) in enumerate(buttons):
        if b.is_clicked(mouse[0], mouse[1]):
            pygame.draw.rect(screen, b.color_light, [b.x, b.y, b.width, b.height])
        else:
            pygame.draw.rect(screen, b.color_dark, [b.x, b.y, b.width, b.height])
        screen.blit(b.text, (b.x + b.tx, b.y + b.height / 2 - 20))


    pygame.display.flip()





