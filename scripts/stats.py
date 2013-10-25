import pytest
import pprint

class MyPlugin:
    def pytest_unconfigure(self, config):
        r = config.pluginmanager.getplugin("terminalreporter")
        stats = r.stats
        pprint.pprint(stats)
        passed = stats.get('passed', [])
        failed = stats.get('error', [])
        print 'passed', len(passed)
        print 'failed', len(failed)
        print 'total', len(passed) + len(failed)

pytest.main("-qq", plugins=[MyPlugin()])
