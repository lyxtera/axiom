/**
 * Axiom Documentation Scripts
 * All JavaScript functionality for the documentation site
 */

document.addEventListener('DOMContentLoaded', function() {
  // Initialize highlight.js for code blocks
  document.querySelectorAll('pre code').forEach((block) => {
    hljs.highlightBlock(block);
  });

  // Handle smooth scrolling to anchors
  initSmoothScrolling();
  
  // Handle theme toggling
  initThemeToggle();
  
  // Add section anchors to headings for direct linking
  addSectionAnchors();
});

/**
 * Initialize smooth scrolling for anchor links
 */
function initSmoothScrolling() {
  // Get all links that point to anchors on the same page
  const anchorLinks = document.querySelectorAll('a[href^="#"]');
  
  // Add click handler to each link
  anchorLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      // Get the target element
      const targetId = this.getAttribute('href').substring(1);
      const targetElement = document.getElementById(targetId);
      
      if (targetElement) {
        e.preventDefault();
        
        // Calculate position with offset for header
        const headerOffset = 80;
        const elementPosition = targetElement.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
        
        // Smooth scroll to element
        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
        
        // Update URL without scrolling
        history.pushState(null, null, `#${targetId}`);
      }
    });
  });
}

/**
 * Initialize theme toggling
 */
function initThemeToggle() {
  const toggleButton = document.getElementById('theme-toggle');
  if (!toggleButton) return;
  
  const htmlElement = document.documentElement;
  const isDarkMode = htmlElement.classList.contains('dark-mode');
  
  // Set initial button text based on mode
  toggleButton.textContent = isDarkMode ? '🌙' : '☀️';
  toggleButton.setAttribute('title', isDarkMode ? 'Switch to light mode' : 'Switch to dark mode');
  
  // Toggle theme when button is clicked
  toggleButton.addEventListener('click', () => {
    if (htmlElement.classList.contains('dark-mode')) {
      htmlElement.classList.remove('dark-mode');
      toggleButton.textContent = '☀️';
      toggleButton.setAttribute('title', 'Switch to dark mode');
      
      // Change highlight.js theme
      document.getElementById('highlight-theme').href = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/github.min.css';
      
      // Save preference
      localStorage.setItem('theme', 'light');
    } else {
      htmlElement.classList.add('dark-mode');
      toggleButton.textContent = '🌙';
      toggleButton.setAttribute('title', 'Switch to light mode');
      
      // Change highlight.js theme
      document.getElementById('highlight-theme').href = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/github-dark.min.css';
      
      // Save preference
      localStorage.setItem('theme', 'dark');
    }
  });
  
  // Check for saved theme preference
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    if (savedTheme === 'light' && htmlElement.classList.contains('dark-mode')) {
      toggleButton.click();
    } else if (savedTheme === 'dark' && !htmlElement.classList.contains('dark-mode')) {
      toggleButton.click();
    }
  } else {
    // Check user's system preference if no saved preference
    const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (prefersDarkMode && !htmlElement.classList.contains('dark-mode')) {
      toggleButton.click();
    } else if (!prefersDarkMode && htmlElement.classList.contains('dark-mode')) {
      toggleButton.click();
    }
  }
}

/**
 * Add clickable anchor links to headings
 */
function addSectionAnchors() {
  const headings = document.querySelectorAll('h2, h3, h4');
  
  headings.forEach(heading => {
    // Skip if heading already has an anchor
    if (heading.querySelector('.section-anchor')) return;
    
    // Create a slug from the heading text
    const slug = heading.textContent.toLowerCase()
      .replace(/[^\w\s-]/g, '')
      .replace(/\s+/g, '-');
    
    // Set the ID if not already set
    if (!heading.id) {
      heading.id = slug;
    }
    
    // Create the anchor element
    const anchor = document.createElement('a');
    anchor.href = `#${heading.id}`;
    anchor.classList.add('section-anchor');
    anchor.textContent = '#';
    anchor.title = 'Direct link to this section';
    
    // Add the anchor to the heading
    heading.appendChild(anchor);
  });
}

/**
 * Navigate to parent section
 */
function goToParentSection(sectionId) {
  // This function is defined in navigation.js
  // We're just providing a fallback if it's not available
  if (typeof PARENT_MAPPINGS !== 'undefined') {
    if (PARENT_MAPPINGS[sectionId]) {
      window.location.hash = PARENT_MAPPINGS[sectionId];
      return true;
    }
  }
  return false;
} 