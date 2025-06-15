# Variables
JAVAC = javac
JAVA = java
JFLAGS = -Xlint:all
BIN_DIR = bin
SRC_ROOT_PKG = project_biu
MAIN_CLASS = $(SRC_ROOT_PKG).Main

# Source files for the main application
APP_SOURCES = $(shell find $(SRC_ROOT_PKG) -path '$(SRC_ROOT_PKG)/tests' -prune -o -name '*.java' -print)

# Source files for the tests
TEST_SOURCES = $(shell find $(SRC_ROOT_PKG)/tests -name '*.java')

# All sources for compilation
ALL_SOURCES = $(APP_SOURCES) $(TEST_SOURCES)

# Test classes (derived from sources, used for running tests)
# Assuming simple main methods, so we list the main test classes to run
# More robust would be to discover test classes (e.g., if using JUnit annotations)
TEST_CLASSES = \
	project_biu.tests.views.HtmlGraphWriterTest \
	project_biu.tests.servlets.ConfLoaderTest \
	project_biu.tests.servlets.HtmlLoaderTest \
	project_biu.tests.servlets.TopicDisplayerTest

# Default target: compile all Java files (app and tests)
all: compile

compile: $(BIN_DIR) $(ALL_SOURCES)
	@echo "Compiling Java sources (app and tests)..."
	$(JAVAC) $(JFLAGS) -d $(BIN_DIR) $(ALL_SOURCES)
	@echo "Compilation complete."

# Target to create the bin directory
$(BIN_DIR):
	@echo "Creating binary directory: $(BIN_DIR)..."
	@mkdir -p $(BIN_DIR)

# Target to run the main application
run: compile
	@echo "Running application $(MAIN_CLASS)..."
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

# Target to run tests
test: compile
	@echo "Running tests..."
	@for test_class in $(TEST_CLASSES); do \
		echo "Executing $$test_class..."; \
		$(JAVA) -cp $(BIN_DIR) $$test_class; \
		echo ""; \
	done
	@echo "Tests execution completed."

# Target to clean up compiled files and bin directory
clean:
	@echo "Cleaning up..."
	@rm -rf $(BIN_DIR)
	@echo "Cleanup complete."

# Phony targets
.PHONY: all compile run test clean
