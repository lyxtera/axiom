#!/bin/bash

# Axiom Documentation Builder
# This script builds a static HTML file from Markdown documentation files

# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PYTHON_SCRIPT="${SCRIPT_DIR}/build.py"
VENV_DIR="${SCRIPT_DIR}/.venv"

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to create virtual environment and install dependencies
setup_environment() {
    echo "Setting up Python environment..."
    
    # Check if Python is installed
    if ! command_exists python3; then
        echo "Error: Python 3 is required but not installed. Please install Python 3 and try again."
        exit 1
    fi
    
    # Create virtual environment if it doesn't exist
    if [ ! -d "${VENV_DIR}" ]; then
        echo "Creating virtual environment..."
        python3 -m venv "${VENV_DIR}"
        if [ $? -ne 0 ]; then
            echo "Error: Failed to create virtual environment. Please make sure 'venv' module is available."
            exit 1
        fi
    fi
    
    # Activate virtual environment
    if [ "$(uname)" == "Darwin" ] || [ "$(uname)" == "Linux" ]; then
        source "${VENV_DIR}/bin/activate"
    elif [ "$(expr substr $(uname -s) 1 5)" == "MINGW" ] || [ "$(expr substr $(uname -s) 1 5)" == "MSYS" ]; then
        source "${VENV_DIR}/Scripts/activate"
    else
        echo "Error: Unsupported operating system. Please install dependencies manually."
        exit 1
    fi
    
    # Install dependencies
    echo "Installing required Python packages..."
    pip install markdown
    
    if [ $? -ne 0 ]; then
        echo "Error: Failed to install required packages."
        exit 1
    fi
}

# Function to run the build script
run_build() {
    echo "Running documentation build script..."
    python3 "${PYTHON_SCRIPT}"
    
    if [ $? -ne 0 ]; then
        echo "Error: Build script failed."
        exit 1
    fi
    
    echo "Build completed successfully."
}

# Main function
main() {
    cd "${SCRIPT_DIR}"
    
    echo "==== Axiom Documentation Builder ===="
    echo "Working directory: ${SCRIPT_DIR}"
    
    # Check if build script exists
    if [ ! -f "${PYTHON_SCRIPT}" ]; then
        echo "Error: Build script not found at ${PYTHON_SCRIPT}"
        exit 1
    fi
    
    # Setup environment
    setup_environment
    
    # Run build
    run_build
    
    echo "Documentation has been built successfully!"
    echo "Open index.html in your browser to view the documentation."
}

# Run main function
main 