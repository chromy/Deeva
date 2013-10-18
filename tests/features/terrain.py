from lettuce import before, world, after
from selenium import webdriver
import lettuce_webdriver.webdriver

@before.all
def setup_browser():
    try:
        world.browser = webdriver.Chrome()
    except:
        world.browser = webdriver.Firefox()

@after.all
def teardown_browser(total):
    world.browser.quit()
