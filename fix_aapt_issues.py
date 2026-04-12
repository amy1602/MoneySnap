import re
import os

def fix_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Standardize header
    content = re.sub(r"<\?xml version='1.0' encoding='utf-8'\?>", '<?xml version="1.0" encoding="utf-8"?>', content)

    # Find all strings
    pattern = re.compile(r'(<string name="[^"]*">)(.*?)(</string>)', re.DOTALL)
    
    def replacement(match):
        prefix = match.group(1)
        value = match.group(2)
        suffix = match.group(3)
        
        # 1. NEWLINES: Replace literal newlines with \n
        value = value.replace('\r\n', '\n').replace('\r', '\n')
        value = value.replace('\n', '\\n')
        
        # 2. APOSTROPHES: If not already escaped and not wrapped in quotes, escape them
        # Note: If the whole string is wrapped in quotes, we don't need to escape '
        # However, we usually don't wrap in quotes, so let's escape ' if it's not \'
        # Simple approach: Replace all ' with \' if not preceded by \
        fixed_value = ""
        i = 0
        while i < len(value):
            if value[i] == "'" and (i == 0 or value[i-1] != "\\"):
                fixed_value += "\\'"
            else:
                fixed_value += value[i]
            i += 1
        value = fixed_value

        # 3. QUOTES: Replace literal " with \"
        fixed_value = ""
        i = 0
        while i < len(value):
            if value[i] == '"' and (i == 0 or value[i-1] != "\\"):
                fixed_value += '\\"'
            else:
                fixed_value += value[i]
            i += 1
        value = fixed_value

        # 4. AMPERSANDS: & must be &amp; (usually handled by XML parser/writer, but here we work with regex)
        # Check for & not followed by amp; lt; gt; quot; apos;
        value = re.sub(r'&(?!(amp|lt|gt|quot|apos);)', '&amp;', value)

        return prefix + value + suffix

    new_content = pattern.sub(replacement, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

fix_file('app/src/main/res/values/strings.xml')
fix_file('app/src/main/res/values-vi/strings.xml')
