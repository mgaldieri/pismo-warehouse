from aloe import step, world, before, after
from nose.tools import assert_equals, assert_in
from subprocess import call
import requests, os, time

docker_ip = os.environ.get("DOCKER_IP") if os.environ.get("DOCKER_IP") else "127.0.0.1"
BASE_URL = "http://{}:8000".format(docker_ip)


def start_application():
    call("./rundocker.sh")

    max_tries = 30
    try_num = 0
    connected = False
    while not connected:
        if try_num == max_tries:
            raise Exception("Connection with server timed out")
        try:
            requests.get("{}/ping".format(BASE_URL))
            connected = True
        except requests.exceptions.ConnectionError:
            time.sleep(2)  # wait for server warmup
        try_num += 1


def stop_application():
    call("./stopdocker.sh")


@before.each_example
def before_each(scenario, outline, steps):
    world.resp = None
    world.jwt = None
    stop_application()
    time.sleep(3) # wait for server cooldown
    start_application()


@after.each_example
def after_each(scenario, outline, steps):
    world.resp = None
    stop_application()

#
# Given
#
@step(r"the service is running")
def check_server_running(step):
    resp = requests.get("{}/ping".format(BASE_URL))
    assert_equals(200, resp.status_code)


@step(r"the user '(.*)' is logged in with password '(.*)'")
def logged_user(step, email, password):
    user = {
        "email": email,
        "password": password
    }
    resp = requests.post("{}/admin/login".format(BASE_URL), json=user)
    data = resp.json().get("data")
    world.jwt = data.get("jwt")
    assert_in("jwt", data)


#
# When
#
@step(r"I retrieve the user '(.*)' with the password '(.*)'")
def login_user(step, email, password):
    user = {
        "email": email,
        "password": password
    }
    world.resp = requests.post("{}/admin/login".format(BASE_URL), json=user)


@step(r"I try to log out")
def logout_user(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.post("{}/admin/logout".format(BASE_URL), headers=headers)


@step(r"I try to retrieve all products")
def get_products(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.get("{}/admin/products".format(BASE_URL), headers=headers)


@step(r"I try to retrieve a product with id '(.*)'")
def get_product(step, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.get("{}/admin/product/{}".format(BASE_URL, product_id), headers=headers)


@step(r"I try to add '(.*)' unit\(s\) of a product with id '(.*)' to the stock")
def add_product_to_stock(step, qty, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        "qty": int(qty)
    }
    world.resp = requests.put("{}/admin/product/{}".format(BASE_URL, product_id), headers=headers, json=payload)


@step(r"I try to change the '(.*)' to '(.*)' of a product with id '(.*)'")
def change_product_property(step, prop, value, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        prop: int_or_value(value)
    }
    world.resp = requests.post("{}/admin/product/{}".format(BASE_URL, product_id), headers=headers, json=payload)

#
# Then
#
@step(r"I should get a '(.*)' response")
def check_response_code(step, expected_response_code):
    print(world.resp.content)
    assert_equals(world.resp.status_code, int(expected_response_code))


@step(r"the response must contain '(.*)'")
def check_response_contains(step, expected_json_field):
    data = world.resp.json()
    assert_in("data", data)
    assert_in(expected_json_field, data.get("data"))


@step(r"the '(.*)' should be '(.*)' for the product with id '(.*)'")
def check_product_property(step, prop, value, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    resp = requests.get("{}/admin/product/{}".format(BASE_URL, product_id), headers=headers)
    data = resp.json().get("data")
    product = data.get("product")
    assert_equals(product.get(prop), int_or_value(value))


def int_or_value(value):
    try:
        return int(value)
    except ValueError:
        return value
