import os

def get_class_files(path):
    return get_files_with_extension(path, ['.class'])

def get_source_files(path, extensions=None):
    extensions = [".java"] if extensions is None else extensions
    return get_files_with_extension(path, extensions)

def get_files_with_extension(path, extensions):
    extensions = tuple(extensions)
    source_files = []
    for _, _, files in os.walk(path):
        for f in files:
            if f.endswith(extensions):
                 source_files.append(f)
    return source_files


