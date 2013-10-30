from deeva import debug
import tempfile

def test_load_returns_correct_file_content():
    f = tempfile.NamedTemporaryFile(delete=False)
    f.write('Hello\nWorld!\n')
    f.close()
    source = debug.load(f.name)
    assert source[0] == 'Hello\n'
    assert source[1] == 'World!\n'

