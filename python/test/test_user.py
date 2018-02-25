from test.utilities import *
from subticket.api import Subticket


def test_create_user():
   st = random_subticket()
   assert ok(st.create_user())
   assert bad_request(st.create_user())


def test_username_too_long():
    st = Subticket('x' * 65, 'bar')
    assert bad_request(st.create_user())
