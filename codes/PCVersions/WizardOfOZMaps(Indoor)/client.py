import socket
import threading

HEADERSIZE = 10
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.connect(("10.0.0.178", 1234))
print(socket.gethostname())


def handle_server():
    newmsg = True
    msg = ''
    while True:
        try:
            header = server_socket.recv(HEADERSIZE)
            if not header:
                print("Connection closed by the client.")
                break

            msglen = int(header[:HEADERSIZE])
            fullmsg = server_socket.recv(msglen).decode("utf-8")
            print(fullmsg)

        except Exception as e:
            print(f"Error: {e}")
            break


thread = threading.Thread(target=handle_server)

thread.start()

while True:

    py = input("")
    if py != "":
        msg = f'{len(py):<{HEADERSIZE}}' + py
        server_socket.send(bytes(msg, "utf-8"))
