import os
import subprocess
import time
from lettuce import step, world, before, after

@step(r'I have a "(.*)" Java program')
def java_program(step, directory):
    path = os.path.join(world.old_cwd, 'examples', directory)
    os.chdir(path)

@step(r'I run the command "(.*)"')
def run_the_command(step, cmd):
    my_env = os.environ.copy()
    my_env['DEEVA_OPEN_BROWSER'] = 'false'
    my_env['DEEVA_DEBUG'] = 'false'
    my_env['DEEVA_TESTING'] = 'true'
    cmd = cmd.split()
    cmd[0] = os.path.join(world.old_cwd, cmd[0])
    world.p = subprocess.Popen(cmd, env=my_env, stderr=subprocess.PIPE)
    time.sleep(5.0)
    assert world.p.returncode is None

@step(u'I see deeva running')
def is_deeva_up(step):
    step.given('I go to "http://localhost:5000"')
    step.then('I should see "Deeva"')

@step(u'I see "(.*)" on stderr')
def content_in_stderr(step, content):
    assert content in world.p.communicate()[1]

@before.each_scenario
def setup_p(scenario):
    world.p = None

@after.each_scenario
def kill_p(scenario):
    if world.p.poll() is None:
        world.p.kill()
        while world.p.poll() is None:
            pass
