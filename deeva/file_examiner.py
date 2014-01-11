import os

def files_with_extension(path, ext):
    for root, _, files in os.walk(path):
        for f in files:
            _, e = os.path.splitext(f)
            if e == ext:
                yield os.path.join(root, f)

def older_than(path_a, path_b):
    # smaller times are younger
    return os.path.getmtime(path_a) < os.path.getmtime(path_b)

def classes_that_look_out_of_date(source_path):
    classes = list(files_with_extension(source_path, '.class'))
    sources = list(files_with_extension(source_path, '.java'))
    classes.sort()
    sources.sort()
    old_files = [s for c, s in zip(classes, sources) if not older_than(s, c)]
    return old_files


