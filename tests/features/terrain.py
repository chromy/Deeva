from lettuce import before, world, after
from selenium import webdriver
import lettuce_webdriver.webdriver

@before.all
def setup_browser():
    world.browser = webdriver.Chrome()

@after.all
def teardown_browser(total):
    world.browser.quit()
