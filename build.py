#!/usr/bin/env python3
"""
Markdown to HTML pre-renderer for Axiom documentation.
This script converts Markdown files to HTML and embeds them in a single HTML file.
"""

import os
import re
import sys
import json
import traceback
import markdown
from pathlib import Path
from collections import defaultdict
from glob import glob

# Configuration
PAGES_DIR = "pages"
OUTPUT_FILE = "index.html"
TEMPLATE_FILE = "template.html"
TOC_FILE = os.path.join(PAGES_DIR, "toc.md")

# Configuration for section mapping
# This determines which section each page belongs to
# Format: 'file_name_without_extension': 'section_name'
# Note: This is used as a fallback if toc.md is not found
PAGE_SECTION_MAPPING = {
    'introduction': 'Introduction & Overview',
    'getting-started': 'Introduction & Overview',
    'code-classes-overview': 'Introduction & Overview',
    'example-section-comment': 'Introduction & Overview',
    'ruleset-overview': 'Development Guide',
    'ruleset-structure': 'Development Guide',
    'code-generation': 'Development Guide',
    'ruleset-validation': 'Development Guide',
    'rule-overview': 'Development Guide',
    'rule-priority': 'Development Guide',
    'rule-effective-dates': 'Development Guide',
    'rule-context-overview': 'Development Guide',
    'rule-context-operations': 'Development Guide',
    'business-components-overview': 'Development Guide',
    'business-components-definition': 'Development Guide',
    'business-components-usage': 'Development Guide',
    'business-components-implementation': 'Development Guide',
    'business-components-validation': 'Development Guide',
    'rule-orchestrator-overview': 'Development Guide',
    'rule-orchestrator-injection': 'Development Guide',
    'rule-orchestrator-operations': 'Development Guide',
    'rule-testing': 'Testing & Best Practices',
    'best-practices': 'Testing & Best Practices'
}

# Section ID to file mapping - will be dynamically generated
SECTION_FILE_MAPPING = {}

# TOC structure - will be parsed from toc.md
TOC_STRUCTURE = {
    'categories': [],  # List of categories in order
    'subcategories': {},  # Dict mapping categories to subcategories
    'pages': {},  # Dict mapping categories to pages or subcategories to pages
    'page_to_category': {},  # Dict mapping page to its category
    'page_to_subcategory': {}  # Dict mapping page to its subcategory (if any)
}

def parse_toc_file():
    """Parse the TOC file to determine the structure."""
    global TOC_STRUCTURE
    
    if not os.path.exists(TOC_FILE):
        print(f"Warning: TOC file {TOC_FILE} not found. Using PAGE_SECTION_MAPPING instead.")
        return False
    
    toc_content = read_file(TOC_FILE)
    if not toc_content:
        print(f"Warning: Could not read TOC file {TOC_FILE}. Using PAGE_SECTION_MAPPING instead.")
        return False
    
    # Reset TOC structure
    TOC_STRUCTURE = {
        'categories': [],
        'subcategories': {},
        'pages': {},
        'page_to_category': {},
        'page_to_subcategory': {}
    }
    
    # Parse the TOC file line by line
    current_category = None
    current_subcategory = None
    
    for line in toc_content.strip().split("\n"):
        # Skip empty lines and title line
        if not line.strip() or line.strip().startswith("# "):
            continue
        
        indentation = len(line) - len(line.lstrip())
        cleaned_line = line.strip()
        
        # Main category (least indentation)
        if indentation == 0 and cleaned_line.startswith("- "):
            category_name = cleaned_line[2:].strip()
            current_category = category_name
            current_subcategory = None
            
            if current_category not in TOC_STRUCTURE['categories']:
                TOC_STRUCTURE['categories'].append(current_category)
                TOC_STRUCTURE['subcategories'][current_category] = []
                TOC_STRUCTURE['pages'][current_category] = []
        
        # Subcategory or direct page under category (medium indentation)
        elif indentation == 4 and cleaned_line.startswith("- "):
            item = cleaned_line[2:].strip()
            
            # If it's a markdown file, it's a direct page under the category
            if item.endswith(".md"):
                if current_category:
                    TOC_STRUCTURE['pages'][current_category].append(item)
                    TOC_STRUCTURE['page_to_category'][item] = current_category
            # Otherwise, it's a subcategory
            else:
                current_subcategory = item
                if current_category and current_subcategory not in TOC_STRUCTURE['subcategories'][current_category]:
                    TOC_STRUCTURE['subcategories'][current_category].append(current_subcategory)
                    TOC_STRUCTURE['pages'][f"{current_category}:{current_subcategory}"] = []
        
        # Page under subcategory (most indentation)
        elif indentation == 8 and cleaned_line.startswith("- ") and cleaned_line.endswith(".md"):
            page = cleaned_line[2:].strip()
            if current_category and current_subcategory:
                TOC_STRUCTURE['pages'][f"{current_category}:{current_subcategory}"].append(page)
                TOC_STRUCTURE['page_to_category'][page] = current_category
                TOC_STRUCTURE['page_to_subcategory'][page] = current_subcategory
    
    # Print the parsed structure for debugging
    print(f"Successfully parsed TOC file with {len(TOC_STRUCTURE['categories'])} categories")
    for category in TOC_STRUCTURE['categories']:
        print(f"  - {category}")
        
        # Print direct pages under this category
        direct_pages = TOC_STRUCTURE['pages'].get(category, [])
        if direct_pages:
            print(f"    - Direct pages: {', '.join(direct_pages)}")
        
        # Print subcategories and their pages
        subcategories = TOC_STRUCTURE['subcategories'].get(category, [])
        for subcategory in subcategories:
            subpages = TOC_STRUCTURE['pages'].get(f"{category}:{subcategory}", [])
            print(f"    - Subcategory '{subcategory}': {', '.join(subpages)}")
    
    return True

def convert_md_to_html(md_content):
    """Convert markdown content to HTML."""
    # Fix "Back to Previous Section" links before converting to HTML
    md_content, fixed_links = fix_back_links(md_content)
    
    # Configure markdown extensions
    extensions = [
        'markdown.extensions.fenced_code',
        'markdown.extensions.tables',
        'markdown.extensions.codehilite',
        'markdown.extensions.toc'
    ]
    
    # Convert markdown to HTML
    html = markdown.markdown(md_content, extensions=extensions)
    return html, fixed_links

def fix_back_links(md_content):
    """Fix all internal links to use section IDs instead of file paths and remove navigation links."""
    # Create a mapping of file names to section IDs
    file_to_section = {v: k for k, v in SECTION_FILE_MAPPING.items()}
    
    # Find all Markdown-style links that point to .md files
    import re
    pattern = r'\[([^\]]+)\]\(([^)]+\.md)(?:#([^)]+))?\)'
    
    # Track fixed links for reporting
    fixed_links = []
    
    def replace_link(match):
        link_text = match.group(1)
        link_target = match.group(2)
        anchor = match.group(3)  # This could be None if there's no anchor
        
        # Check if this is a navigation link (Back to, Next, Previous)
        if "Back to" in link_text or "Next:" in link_text or "→" in link_text:
            # Remove the navigation link entirely
            fixed_links.append((match.group(0), ""))
            return ""
        
        # Extract the base file name without path
        base_file = os.path.basename(link_target)
        
        # If we have a mapping for this file, replace with section ID
        if base_file in file_to_section:
            section_id = file_to_section[base_file]
            if anchor:
                # If there's an anchor, append it to the section ID
                new_link = f'[{link_text}](#{section_id}-{anchor})'
                fixed_links.append((match.group(0), new_link))
                return new_link
            else:
                new_link = f'[{link_text}](#{section_id})'
                fixed_links.append((match.group(0), new_link))
                return new_link
        
        # Otherwise leave it as is
        return match.group(0)
    
    # Replace all matching links
    fixed_content = re.sub(pattern, replace_link, md_content)
    
    # Also handle "Back to Previous Section" links specifically
    back_pattern = r'\[← Back to Previous Section\]\(([^)]+\.md)\)'
    
    def replace_back_link(match):
        # Remove the back link entirely
        fixed_links.append((match.group(0), ""))
        return ""
    
    # Replace all "Back to Previous Section" links
    fixed_content = re.sub(back_pattern, replace_back_link, fixed_content)
    
    # Remove any lingering navigation links that might use different formats
    nav_patterns = [
        r'\[← Back to [^\]]+\]\([^)]+\)',
        r'\[Next: [^\]]+\s→\]\([^)]+\)'
    ]
    
    for pattern in nav_patterns:
        fixed_content = re.sub(pattern, "", fixed_content)
    
    return fixed_content, fixed_links

def extract_section_comment(md_content):
    """Extract the section name from an HTML comment in the markdown content."""
    # Look for the section comment pattern {[//]: # (section-name)}
    pattern = r'\{\[//\]: # \((.*?)\)\}'
    match = re.search(pattern, md_content)
    if match:
        return match.group(1)
    return None

def extract_back_link_target(md_content):
    """Extract the target file from a "Back to Previous Section" link."""
    # Look for the back link pattern
    pattern = r'\[← Back to Previous Section\]\(([^)]+\.md)\)'
    match = re.search(pattern, md_content)
    if match:
        return os.path.basename(match.group(1))
    return None

def discover_markdown_files():
    """Discover all markdown files in the pages directory and build the SECTION_FILE_MAPPING."""
    global SECTION_FILE_MAPPING
    
    # Find all markdown files in the pages directory
    md_files = []
    for filename in os.listdir(PAGES_DIR):
        if filename.endswith(".md") and filename != "toc.md":  # Skip toc.md itself
            md_files.append(filename)
    
    # Generate section IDs for each file
    for md_file in md_files:
        # Remove .md extension to get the base name
        base_name = os.path.splitext(md_file)[0]
        # Use the base name as the section ID
        section_id = base_name
        # Add to the mapping
        SECTION_FILE_MAPPING[section_id] = md_file
    
    print(f"Discovered {len(md_files)} markdown files")
    return md_files

def get_section_category(section_id):
    """Get the category a section belongs to based on the TOC structure or fallback mapping."""
    # Try to get from TOC_STRUCTURE first
    md_file = SECTION_FILE_MAPPING.get(section_id, '')
    if md_file in TOC_STRUCTURE['page_to_category']:
        return TOC_STRUCTURE['page_to_category'][md_file]
    
    # Fallback to PAGE_SECTION_MAPPING
    base_name = os.path.splitext(md_file)[0]
    return PAGE_SECTION_MAPPING.get(base_name, 'Other')

def get_section_subcategory(section_id):
    """Get the subcategory a section belongs to based on the TOC structure."""
    md_file = SECTION_FILE_MAPPING.get(section_id, '')
    return TOC_STRUCTURE['page_to_subcategory'].get(md_file, None)

def build_page_hierarchy():
    """Build a hierarchy of pages based on back links and section comments."""
    # Initialize data structures
    page_hierarchy = defaultdict(list)
    sections = {}
    page_titles = {}
    
    # Get file to section ID mapping
    file_to_section = {v: k for k, v in SECTION_FILE_MAPPING.items()}
    
    # First pass: extract section comments and titles
    for md_file in SECTION_FILE_MAPPING.values():
        md_path = os.path.join(PAGES_DIR, md_file)
        md_content = read_file(md_path)
        
        if md_content:
            # Extract section from comment
            section = extract_section_comment(md_content)
            if section:
                sections[md_file] = section
            
            # Extract title from first h1
            title_match = re.search(r'^# (.*?)$', md_content, re.MULTILINE)
            if title_match:
                page_titles[md_file] = title_match.group(1)
            else:
                section_id = file_to_section.get(md_file)
                if section_id:
                    page_titles[md_file] = section_id.replace('-', ' ').title()
    
    # Second pass: build hierarchy based on back links
    for md_file in SECTION_FILE_MAPPING.values():
        md_path = os.path.join(PAGES_DIR, md_file)
        md_content = read_file(md_path)
        
        if md_content:
            # Extract back link target
            parent_file = extract_back_link_target(md_content)
            if parent_file:
                # If the parent file exists in our mapping, add this page as its child
                if parent_file in file_to_section:
                    parent_section = file_to_section[parent_file]
                    child_section = file_to_section[md_file]
                    page_hierarchy[parent_section].append(child_section)
    
    # Return the hierarchy and metadata
    return {
        'hierarchy': dict(page_hierarchy),
        'sections': sections,
        'titles': page_titles
    }

def generate_toc_new(categories, subcategories):
    """Generate a table of contents HTML based on the TOC structure."""
    # Start building the TOC
    toc_html = '<div class="left-toc">\n'
    toc_html += '<h4>Table of Contents</h4>\n'
    
    # Process each category
    for category in categories:
        toc_html += f'<div class="section-heading">{category}</div>\n<ul>\n'
        
        # Direct pages in this category (no subcategory)
        direct_pages = TOC_STRUCTURE['pages'].get(category, [])
        
        for md_file in direct_pages:
            section_id = md_file.replace(".md", "")
            title = section_id.replace("-", " ").title()
            toc_html += f'<li><a href="#{section_id}">{title}</a></li>\n'
        
        # Process subcategories
        category_subcategories = subcategories.get(category, [])
        if category_subcategories:
            for subcategory in category_subcategories:
                # Add subcategory header
                toc_html += f'<li class="subcategory">{subcategory}</li>\n'
                
                # Start subcategory list
                toc_html += '<ul class="subcategory-list">\n'
                
                # Pages in this subcategory
                subcategory_key = f"{category}:{subcategory}"
                subcategory_pages = TOC_STRUCTURE['pages'].get(subcategory_key, [])
                
                for md_file in subcategory_pages:
                    section_id = md_file.replace(".md", "")
                    title = section_id.replace("-", " ").title()
                    toc_html += f'<li><a href="#{section_id}" class="left-toc-h2">{title}</a></li>\n'
                
                # End subcategory list
                toc_html += '</ul>\n'
        
        toc_html += '</ul>\n'
    
    toc_html += '</div>'
    return toc_html

def read_file(file_path):
    """Read a file and return its content."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        print(f"Error reading file {file_path}: {e}")
        return None

def write_file(file_path, content):
    """Write content to a file."""
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    except Exception as e:
        print(f"Error writing to file {file_path}: {e}")
        return False

def create_template_if_not_exists():
    """Create the template file if it doesn't exist."""
    if not os.path.exists(TEMPLATE_FILE):
        # Try reading from index.html first
        if os.path.exists(OUTPUT_FILE):
            index_content = read_file(OUTPUT_FILE)
            if index_content:
                # Replace the content section with a placeholder
                content_pattern = r'<main class="content" id="content">.*?</main>'
                template_content = re.sub(
                    content_pattern, 
                    '<main class="content" id="content"><!-- CONTENT_PLACEHOLDER --></main>',
                    index_content,
                    flags=re.DOTALL
                )
                
                # Replace left navigation with a placeholder
                toc_pattern = r'<nav class="left-toc".*?</nav>'
                template_content = re.sub(
                    toc_pattern,
                    '<!-- TOC_PLACEHOLDER -->',
                    template_content,
                    flags=re.DOTALL
                )
                
                # Update the JavaScript to use dynamic parent mappings
                script_pattern = r'const PARENT_MAPPINGS = \{.*?\};'
                template_content = re.sub(
                    script_pattern,
                    'const PARENT_MAPPINGS = {}; // Will be auto-generated during build',
                    template_content,
                    flags=re.DOTALL
                )
                
                # Write the template file
                if write_file(TEMPLATE_FILE, template_content):
                    print(f"Created template file: {TEMPLATE_FILE}")
                    return True
                else:
                    print(f"Error: Failed to write template file {TEMPLATE_FILE}")
                    sys.exit(1)
            else:
                print(f"Error reading file {OUTPUT_FILE}: Could not read file")
        
        # If we get here, either index.html doesn't exist or we couldn't read it
        # Create a minimal template from scratch
        print("Creating template from scratch...")
        minimal_template = """<!DOCTYPE html>
<html lang="en" class="dark-mode">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Axiom Business Rules Developer Guide</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/github-dark.min.css" id="highlight-theme">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js"></script>
    <link rel="stylesheet" href="static/styles.css">
</head>
<body>
    <header>
        <div class="header-content">
            <h1 class="header-title">Axiom Business Rules Developer Guide</h1>
            <div class="toolbar">
                <button class="theme-toggle" id="theme-toggle" aria-label="Toggle dark/light mode">🌙</button>
            </div>
        </div>
    </header>
    
    <div class="main-container">
        <div class="content-wrapper">
            <div class="left-toc-container">
                <!-- TOC_PLACEHOLDER -->
            </div>
            <main class="content" id="content">
                <!-- CONTENT_PLACEHOLDER -->
            </main>
        </div>
    </div>
    
    <script src="static/theme-toggle.js"></script>
    <script src="static/navigation.js"></script>
    <script src="static/scripts.js"></script>
</body>
</html>
"""
        
        # Write the minimal template
        if write_file(TEMPLATE_FILE, minimal_template):
            print(f"Created template file: {TEMPLATE_FILE}")
            return True
        else:
            print(f"Error: Failed to write template file {TEMPLATE_FILE}")
            sys.exit(1)
    
    return True

def determine_section_order():
    """Determine the order of sections based on TOC structure."""
    section_order = []
    
    # Process each category from TOC_STRUCTURE
    for category in TOC_STRUCTURE['categories']:
        # Direct pages in this category (no subcategory)
        direct_pages = TOC_STRUCTURE['pages'].get(category, [])
        
        for md_file in direct_pages:
            section_id = [sid for sid, file in SECTION_FILE_MAPPING.items() if file == md_file]
            if section_id:
                if section_id[0] not in section_order:
                    section_order.append(section_id[0])
        
        # Process subcategories
        for subcategory in TOC_STRUCTURE['subcategories'].get(category, []):
            # Pages in this subcategory
            subcategory_pages = TOC_STRUCTURE['pages'].get(f"{category}:{subcategory}", [])
            
            for md_file in subcategory_pages:
                section_id = [sid for sid, file in SECTION_FILE_MAPPING.items() if file == md_file]
                if section_id:
                    if section_id[0] not in section_order:
                        section_order.append(section_id[0])
    
    # Add any remaining pages not explicitly in TOC
    for section_id in SECTION_FILE_MAPPING.keys():
        if section_id not in section_order:
            section_order.append(section_id)
    
    print(f"Determined order of {len(section_order)} sections based on TOC file")
    return section_order

def build_html():
    """Build the HTML file from Markdown files."""
    try:
        print("Building HTML...")
        
        # Discover all markdown files
        md_files = discover_markdown_files()
        print(f"Discovered {len(md_files)} markdown files.")
        
        # Parse the table of contents
        parse_toc_file()
        categories = TOC_STRUCTURE['categories']
        subcategories = TOC_STRUCTURE['subcategories']
        print(f"Parsed TOC with {len(categories)} categories and {sum(len(subs) for subs in subcategories.values())} subcategories.")
        
        # Generate the table of contents
        toc = generate_toc_new(categories, subcategories)
        
        # Generate parent mappings dictionary
        parent_mappings = {}
        
        # Build HTML for each section in the correct order
        content_html = ""
        for section_id in categories:
            category_name = section_id
            content_html += f'<section id="{section_id}" class="category-section">\n'
            content_html += f'<h2>{category_name}</h2>\n'
            
            # Add subcategories if any
            if section_id in subcategories:
                for subsection_id in subcategories[section_id]:
                    # Add to parent mappings
                    parent_mappings[subsection_id] = section_id
                    
                    content_html += f'<section id="{subsection_id}" class="subcategory-section">\n'
                    content_html += f'<h3>{subsection_id}</h3>\n'
                    
                    # Find all md files for this subcategory
                    subsection_key = f"{section_id}:{subsection_id}"
                    subsection_files = TOC_STRUCTURE['pages'].get(subsection_key, [])
                    
                    for md_file in subsection_files:
                        file_id = md_file.replace(".md", "")
                        
                        # Add to parent mappings
                        parent_mappings[file_id] = subsection_id
                        
                        # Add section anchor
                        content_html += f'<section id="{file_id}" class="content-section">\n'
                        
                        # Convert markdown to HTML and add to content
                        md_content = read_file(os.path.join(PAGES_DIR, md_file))
                        if md_content:
                            # Fix links in markdown to point to section anchors instead of md files
                            fixed_content, fixed_links = fix_back_links(md_content)
                            # Convert markdown to HTML
                            section_html = markdown.markdown(fixed_content, extensions=['fenced_code', 'tables'])
                            content_html += section_html
                        else:
                            print(f"Warning: Empty content in {md_file}")
                        
                        content_html += '</section>\n'
                    
                    content_html += '</section>\n'
            
            # Add direct pages for this category
            direct_pages = TOC_STRUCTURE['pages'].get(section_id, [])
            
            for md_file in direct_pages:
                file_id = md_file.replace(".md", "")
                
                # Add to parent mappings
                parent_mappings[file_id] = section_id
                
                # Add section anchor
                content_html += f'<section id="{file_id}" class="content-section">\n'
                
                # Convert markdown to HTML and add to content
                md_content = read_file(os.path.join(PAGES_DIR, md_file))
                if md_content:
                    # Fix links in markdown to point to section anchors instead of md files
                    fixed_content, fixed_links = fix_back_links(md_content)
                    # Convert markdown to HTML
                    section_html = markdown.markdown(fixed_content, extensions=['fenced_code', 'tables'])
                    content_html += section_html
                else:
                    print(f"Warning: Empty content in {md_file}")
                
                content_html += '</section>\n'
            
            content_html += '</section>\n'
        
        # Update navigation.js with parent mappings
        navigation_js_path = 'static/navigation.js'
        if os.path.exists(navigation_js_path):
            nav_js_content = '// Navigation utilities\n\n'
            nav_js_content += f'const PARENT_MAPPINGS = {json.dumps(parent_mappings, indent=2)};\n\n'
            nav_js_content += 'function goToParentSection(sectionId) {\n'
            nav_js_content += '    if (PARENT_MAPPINGS[sectionId]) {\n'
            nav_js_content += '        window.location.hash = PARENT_MAPPINGS[sectionId];\n'
            nav_js_content += '        return true;\n'
            nav_js_content += '    }\n'
            nav_js_content += '    return false;\n'
            nav_js_content += '}\n'
            
            # Write the updated navigation.js
            write_file(navigation_js_path, nav_js_content)
            print(f"Updated parent mappings in navigation.js with {len(parent_mappings)} entries.")
        
        # Generate complete HTML structure directly
        html_content = f'''<!DOCTYPE html>
<html lang="en" class="dark-mode">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Axiom Business Rules Developer Guide</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/github-dark.min.css" id="highlight-theme">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js"></script>
    <link rel="stylesheet" href="static/styles.css">
</head>
<body>
    <header>
        <div class="header-content">
            <h1 class="header-title">Axiom Business Rules Developer Guide</h1>
            <div class="toolbar">
                <button class="theme-toggle" id="theme-toggle" aria-label="Toggle dark/light mode">🌙</button>
            </div>
        </div>
    </header>
    
    <div class="main-container">
        <div class="content-wrapper">
            <div class="left-toc-container">
                {toc}
            </div>
            <main class="content" id="content">
                {content_html}
            </main>
        </div>
    </div>
    
    <script src="static/navigation.js"></script>
    <script src="static/scripts.js"></script>
</body>
</html>'''
        
        # Write the final HTML
        if write_file(OUTPUT_FILE, html_content):
            print(f"Successfully generated {OUTPUT_FILE}")
            return True
        else:
            print(f"Error: Failed to write output file {OUTPUT_FILE}")
            sys.exit(1)
    
    except Exception as e:
        print(f"Error building HTML: {e}")
        traceback.print_exc()
        sys.exit(1)

def main():
    """Main function."""
    print("Building Axiom documentation...")
    
    # Create pages directory if it doesn't exist
    if not os.path.exists(PAGES_DIR):
        os.makedirs(PAGES_DIR)
        print(f"Created directory: {PAGES_DIR}")
    
    # Create static directory if it doesn't exist
    static_dir = 'static'
    if not os.path.exists(static_dir):
        os.makedirs(static_dir)
        print(f"Created directory: {static_dir}")
    
    # Build the HTML
    build_html()

if __name__ == "__main__":
    main() 
