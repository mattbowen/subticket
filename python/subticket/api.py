import requests
import urllib.parse

def q(path_segment):
    return urllib.parse.quote(path_segment, safe='')

class Subticket:

    def __init__(self, username, password, host = 'http://localhost:8080'):
        self.host = host
        self.session = requests.session()
        self.username = username
        self.password = password

    def login(self):
        return self.session.post(f"{self.host}/login", json={'username': self.username, 'password': self.password})
    def create_user(self):
        return self.session.put(f"{self.host}/user/{q(self.username)}", json={'password': self.password})
