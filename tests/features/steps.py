import subprocess
from lettuce import step, world, before, after

@step(r'I have a simple Java program')
def simple_java_program(step):
    pass

@step(r'I run the command "(.*)"')
def run_the_command(step, cmd):
    world.p = subprocess.Popen(cmd.split())

@step(u'I see deeva running')
def is_deeva_up(step):
    step.given('I go to "http://localhost:5000"')
    step.then('I should see "Deeva"')

@before.each_scenario
def setup_p(scenario):
    world.p = None

@after.each_scenario
def kill_p(scenario):
    if world.p != None:
        world.p.terminate()
