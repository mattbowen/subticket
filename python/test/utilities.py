import random, string
from subticket.api import Subticket

def random_string():
    return ''.join(random.choice(string.printable) for i in range(64))

def random_subticket():
    return Subticket(random_string(), random_string())

def random_email():
    return random_string() + '@subticket.com'

def get_api():
    t = random_subticket()
    t.create_user(random_email())
    return t

def ok(resp):
    return resp.status_code == 200
def bad_request(resp):
    return resp.status_code == 400
def forbidden(resp):
    return resp.status_code == 403
def not_found(resp):
    return resp.status_code == 404

