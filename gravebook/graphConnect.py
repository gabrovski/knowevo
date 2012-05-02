import socket, os
from knowevo.settings import MEDIA_ROOT

HOST, PORT = 'localhost', 62541;

def sendName(name):
    sd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    name = name
    for fname in os.listdir(MEDIA_ROOT):
        if name == fname.split('.')[0]:
            return

    try:
        sd.connect((HOST, PORT))
        sd.sendall(name+'\n')
        
        res = sd.recv(32)
        print res

    finally:
        sd.close()

if __name__ == '__main__':
    sendName('Alan Turing')
