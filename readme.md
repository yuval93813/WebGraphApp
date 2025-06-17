# WebGraphApp

A Java-based web application for creating, visualizing, and managing computational graphs using a publish-subscribe messaging system with interactive web interface.

## 🚀 Features

- **Dynamic Graph Creation**: Build computational graphs using configuration files
- **Multi-Agent System**: Support for various computational agents (PlusAgent, MaxAgent, MinAgent, MultiplyAgent, AverageAgent, IncAgent)
- **Real-time Visualization**: Interactive SVG-based graph visualization in web browser
- **Publish-Subscribe Architecture**: Event-driven messaging system between topics and agents
- **Web Interface**: Complete web-based interface for graph management and visualization
- **HTTP Server**: Custom multi-threaded HTTP server implementation
- **File Upload**: Configuration file upload and processing capabilities
- **Extended Testing Suite**: Additional validation and testing components in main_trains folder

## 🏗️ Architecture

### Core Components

- **Graph Engine**: Topic and Agent management with publish-subscribe messaging
- **HTTP Server**: Custom implementation with servlet-based request handling
- **Web Interface**: HTML/CSS/JavaScript frontend for graph interaction
- **Visualization**: SVG-based dynamic graph rendering
- **Testing & Validation**: Extended test suite with additional checks and validations

### Supported Agents

- **PlusAgent**: Adds two input values
- **MaxAgent**: Finds maximum of two input values
- **MinAgent**: Finds minimum of two input values
- **MultiplyAgent**: Multiplies two input values
- **AverageAgent**: Calculates average of two input values
- **IncAgent**: Increments input value by 1

## 📋 Prerequisites

- Java 11 or higher
- Web browser (Chrome, Firefox, Safari, Edge)
- Command line terminal

## 🛠️ Installation & Setup

### Main Application (project_biu)

1. **Clone or download the project**
   ```bash
   git clone <repository-url>
   cd WebGraphApp/project_biu
   ```

2. **Compile the project**
   ```bash
   javac -d . *.java server/*.java servlets/*.java graph/*.java views/*.java
   ```

3. **Start the server**
   ```bash
   java Main
   ```

4. **Access the application**
   - Open your web browser
   - Navigate to: `http://localhost:8080/app/index.html`

### Extended Testing Suite (main_trains)

The `main_trains` folder contains additional validation and testing components with enhanced checks:

1. **Navigate to testing suite**
   ```bash
   cd WebGraphApp/main_trains
   ```

2. **Compile testing components**
   ```bash
   javac -d . *.java
   ```

3. **Run extended tests**
   ```bash
   java TestRunner
   ```

## 📁 Project Structure

```
WebGraphApp/
├── project_biu/                     # Main application
│   ├── Main.java                    # Application entry point
│   ├── server/                      # HTTP server implementation
│   │   ├── HTTPServer.java          # Server interface
│   │   ├── MyHTTPServer.java        # Server implementation
│   │   └── RequestParser.java       # HTTP request parser
│   ├── servlets/                    # Request handlers
│   │   ├── Servlet.java             # Servlet interface
│   │   ├── ConfLoader.java          # Configuration file loader
│   │   ├── GraphUpdateServlet.java  # Graph visualization handler
│   │   ├── HtmlLoader.java          # Static file server
│   │   └── TopicDisplayer.java      # Topic information display
│   ├── graph/                       # Graph engine
│   │   ├── Agent.java               # Agent interface
│   │   ├── Topic.java               # Topic implementation
│   │   ├── Message.java             # Message data structure
│   │   ├── Graph.java               # Graph container
│   │   ├── TopicManagerSingleton.java # Topic management
│   │   └── [Various Agent implementations]
│   └── views/                       # Visualization components
│       └── HtmlGraphWriter.java     # SVG graph renderer
├── main_trains/                     # 🆕 Extended testing suite
│   ├── TestRunner.java              # Test execution framework
│   ├── ValidationSuite.java         # Additional validation checks
│   ├── GraphTester.java             # Graph functionality tests
│   ├── AgentValidator.java          # Agent behavior validation
│   └── [Additional test components]
├── html_files/                      # Web interface files
│   ├── index.html                   # Main application page
│   ├── graph.html                   # Graph visualization page
│   └── [CSS/JS files]
└── config_files/                    # Sample configuration files
    ├── test_config.conf
    ├── test_config2.conf
    └── test_config3.conf
```

## 🧪 Testing & Validation

### Main Application Tests (project_biu)
- Basic functionality testing
- Web interface validation
- HTTP server testing

### Extended Test Suite (main_trains)
The `main_trains` folder provides comprehensive testing with additional checks:

- **Enhanced Agent Validation**: Extended validation of agent behavior and edge cases
- **Graph Integrity Checks**: Advanced graph structure and connectivity validation
- **Performance Testing**: Load testing and performance benchmarks
- **Error Handling Tests**: Comprehensive error scenario testing
- **Integration Tests**: End-to-end system integration validation
- **Stress Testing**: High-load and concurrent operation testing

#### Running Extended Tests

```bash
cd main_trains

# Run all validation tests
java TestRunner --all

# Run specific test suites
java TestRunner --agents        # Agent-specific tests
java TestRunner --graph         # Graph structure tests
java TestRunner --performance   # Performance benchmarks
java TestRunner --integration   # Integration tests
```

#### Test Reports
Extended tests generate detailed reports including:
- Test execution summaries
- Performance metrics
- Coverage analysis
- Error logs and debugging information

## 🎯 Usage

### 1. Creating a Configuration File

Create a `.conf` file with agent definitions:

```properties
# Basic configuration example
graph.PlusAgent
A,B
C

graph.MaxAgent
A,B
D

graph.IncAgent
C
E

graph.MultiplyAgent
D,E
F
```

**Format:**
- Line 1: Agent class (e.g., `graph.PlusAgent`)
- Line 2: Input topics (comma-separated)
- Line 3: Output topic
- Empty line between agents

### 2. Loading Configuration

1. **Via Web Interface:**
   - Go to `http://localhost:8080/app/index.html`
   - Click "Choose File" and select your `.conf` file
   - Click "Upload Configuration"

2. **Via HTTP POST:**
   ```bash
   curl -X POST -F "confFile=@config.conf" http://localhost:8080/upload
   ```

### 3. Publishing Values to Topics

- **Web Interface:** Use the topic input forms on the main page
- **HTTP GET:** `http://localhost:8080/publish?topic=A&msg=10`

### 4. Viewing the Graph

- Navigate to: `http://localhost:8080/graph`
- The graph updates automatically when configuration or values change
- **Topics** appear as rectangles (cyan/teal color)
- **Agents** appear as circles (red/coral color)
- **Arrows** show data flow direction

## 🔧 Configuration Examples

### Simple Math Operations
```properties
graph.PlusAgent
A,B
SUM_AB

graph.MultiplyAgent
A,B
PRODUCT_AB

graph.MaxAgent
SUM_AB,PRODUCT_AB
RESULT
```

### Complex Processing Pipeline
```properties
graph.PlusAgent
A,B
C

graph.MultiplyAgent
A,B
D

graph.IncAgent
C
E

graph.IncAgent
D
F

graph.AverageAgent
E,F
FINAL_RESULT
```

## 🌐 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/app/` | Serve static HTML files |
| POST | `/upload` | Upload configuration file |
| GET | `/publish` | Publish message to topic |
| GET | `/graph` | Get graph visualization |

### Query Parameters

- **`/publish`**: `?topic=<topic_name>&msg=<message_value>`
- **`/graph`**: No parameters (returns complete graph)

## 🎨 Visualization Features

- **Responsive Layout**: Graphs automatically adjust to content size
- **Node Types**: 
  - Topics: Rectangular nodes with topic name and current value
  - Agents: Circular nodes with agent name only
- **Interactive Elements**: Click nodes for detailed information
- **Real-time Updates**: Graph refreshes when data changes
- **Professional Styling**: Clean, modern appearance with proper spacing

## 🔄 Message Flow

1. **Input**: Values published to input topics (A, B, etc.)
2. **Processing**: Agents subscribe to topics and process incoming messages
3. **Propagation**: Agents publish results to output topics
4. **Cascade**: Output topics trigger downstream agents
5. **Visualization**: Graph displays current state and values

## 🛠️ Development

### Adding New Agent Types

1. **Create agent class** implementing `Agent` interface:
   ```java
   public class CustomAgent implements Agent {
       // Implementation
   }
   ```

2. **Add to configuration**: Use `graph.CustomAgent` in config files

3. **Test with extended suite**: Run validation tests in `main_trains`

### Extending the Web Interface

- Modify files in `html_files/` directory
- Static files served automatically by `HtmlLoader` servlet
- Add new servlets for additional functionality

### Testing New Features

1. **Basic testing**: Use standard project_biu tests
2. **Extended validation**: Run comprehensive tests in main_trains folder
3. **Performance analysis**: Use built-in benchmarking tools

### Generating Documentation

```bash
# Generate JavaDoc for main application
cd project_biu
javadoc -d javadoc -sourcepath . server/*.java servlets/*.java graph/*.java views/*.java -private -author -version

# Generate JavaDoc for testing suite
cd ../main_trains
javadoc -d test_javadoc -sourcepath . *.java -private -author -version

# Open documentation
start javadoc/index.html
```

## 🐛 Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   - Change port in `Main.java` constructor: `new MyHTTPServer(8081, 5)`

2. **Configuration file not loading**
   - Check file format (no extra spaces, correct line breaks)
   - Ensure agent classes exist in classpath
   - Run validation tests in `main_trains` for detailed diagnostics

3. **Graph not displaying**
   - Verify server is running on correct port
   - Check browser console for JavaScript errors
   - Ensure configuration is loaded successfully
   - Use extended test suite for comprehensive validation

4. **Agents not processing**
   - Check that input topics receive values
   - Verify agent subscription setup
   - Check server logs for errors
   - Run agent validation tests in `main_trains`

### Debug Mode

- **Basic logging**: Check server console output and `server_error.log` file
- **Extended diagnostics**: Use test reports from `main_trains` validation suite
- **Performance monitoring**: Run performance tests for bottleneck identification

### Using Extended Test Suite for Debugging

```bash
cd main_trains

# Diagnose specific issues
java TestRunner --debug --component=agents    # Debug agent issues
java TestRunner --debug --component=graph     # Debug graph issues
java TestRunner --debug --component=server    # Debug server issues

# Generate diagnostic reports
java TestRunner --report --output=debug_report.html
```

## 📄 License

This project is developed for educational purposes as part of advanced programming coursework.

## 👥 Authors

- **Almog Sharoni**
- **Yuval Harary**

## 🤝 Contributing

This is an academic project. For suggestions or improvements, please contact the authors.

## 📝 Version History

- **v1.0**: Initial release with basic functionality
- **v1.1**: Added extended testing suite in main_trains folder with comprehensive validation checks

---

**Version**: 1.1  
**Last Updated**: December 2024  
**Testing Suite**: Enhanced validation available in `main_trains/`