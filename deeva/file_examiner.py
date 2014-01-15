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
    classes_without_ext = set((c[:-len('.class')] for c in classes))
    sources_without_ext = set((c[:-len('.java')] for c in sources))
    matched_files = classes_without_ext.intersection(sources_without_ext)
    pairs = [(f + '.java', f + '.class') for f in matched_files]
    old_files = [s for s, c in pairs if not older_than(s, c)]
    return old_files


