import socket

HOST, PORT = 'localhost', 62541;

def sendName(name):
    sd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sd.connect((HOST, PORT))
        sd.sendall(name)
    finally:
        sd.close()

if __name__ == '__main__':
    sendName('Alan Turing')