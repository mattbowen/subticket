from test.utilities import *
from subticket.api import Subticket


def test_create_user():
   st = random_subticket()
   assert ok(st.create_user(random_email()))
   assert bad_request(st.create_user(random_email()))
   assert ok(st.login())


def test_username_too_long():
    st = Subticket('x' * 65, 'bar')
    assert bad_request(st.create_user(random_email()))

def test_bad_request():
   st = random_subticket()
   assert bad_request(st.session.put(f"{st.host}/user/ab%20%", json={'password': st.password, 'email': random_email()}))

