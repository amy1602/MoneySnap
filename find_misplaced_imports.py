import os

def check_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
    
    # We find if ANY import follows ANY non-import, non-package, non-comment, non-empty line.
    found_code = False
    for i, line in enumerate(lines):
        trimmed = line.strip()
        if not trimmed: continue
        if trimmed.startswith('//') or trimmed.startswith('/*') or trimmed.startswith('*'): continue
        if trimmed.startswith('package '): continue
        if trimmed.startswith('import '):
            if found_code:
                print(f"ERROR: {filepath}:{i+1}: {trimmed}")
        else:
            # It's code, or a class/fun declaration
            found_code = True

for root, dirs, files in os.walk('app/src/main/java/com/moneysnap/presentation'):
    for file in files:
        if file.endswith('.kt'):
            check_file(os.path.join(root, file))
