#!/bin/bash

# BPark System Launcher Demo Script
# This script demonstrates how to run the BPark system with the new launcher

echo "==========================================="
echo "         BPark System Launcher Demo"
echo "==========================================="
echo

echo "The BPark system now includes a unified launcher!"
echo
echo "Usage options:"
echo
echo "1. Unified Launcher (Recommended):"
echo "   java client.BParkLauncher"
echo "   - Provides GUI to choose between Client Portal and Kiosk Terminal"
echo
echo "2. Direct Client Portal:"
echo "   java client.BParkClientApp"
echo "   - Launches directly to client login screen"
echo
echo "3. Direct Kiosk Terminal:"
echo "   java client.BParkKioskApp"
echo "   - Launches directly to kiosk interface"
echo

echo "==========================================="
echo "              Features"
echo "==========================================="
echo
echo "✓ Unified entry point for the entire system"
echo "✓ Clean mode selection interface"
echo "✓ Reduced code duplication (~60 lines eliminated)"
echo "✓ Better separation of concerns"
echo "✓ Enhanced maintainability"
echo "✓ Consistent styling and behavior"
echo

echo "==========================================="
echo "            System Requirements"
echo "==========================================="
echo
echo "• Java 17+ with JavaFX"
echo "• MySQL connector (mysql-connector-java-8.0.13.jar)"
echo "• BPark server running on localhost:5555 (configurable)"
echo

echo "For more information, see:"
echo "• LAUNCHER_README.md - Detailed implementation guide"
echo "• ARCHITECTURE.md - System architecture overview"
echo

echo "==========================================="