# Computation Graph System - Interactive Web UI

A complete web-based system for visualizing and managing computation graphs with an interactive UI.

## 🎯 **SYSTEM STATUS: FULLY OPERATIONAL** ✅

The Interactive Web UI with Computation Graph System is **100% complete and working**!

### 🚀 **Quick Start (VERIFIED WORKING)**

1. **Start the server**:
   ```bash
   cd project_biu
   java -cp . Main
   ```

2. **Access the web interface**:
   ```
   http://localhost:8080/app/index.html
   ```

3. **All endpoints tested and working**:
   - ✅ Static file serving: `/app/index.html`, `/app/form.html`, `/app/graph.html`
   - ✅ Topic publishing: `/publish?topic=test&message=hello`
   - ✅ File upload: `/upload` (POST)

## 🚀 Quick Start

### Option 1: Using the Startup Script
```bash
./start_server.sh
```

### Option 2: Manual Start
```bash
cd project_biu
find . -name "*.java" -print0 | xargs -0 javac -cp .
java -cp . Main
```

Then open your browser to: **http://localhost:8080/app/index.html**

## 📋 System Overview

The system consists of:

### 🖥️ Web Interface (`html_files/`)
- **`index.html`**: Main dashboard with three-panel layout
- **`form.html`**: Control forms for uploading configs and sending messages
- **`graph.html`**: Interactive computation graph visualization
- **`temp.html`**: Placeholder content

### ⚙️ Server Components (`project_biu/`)
- **`Main.java`**: HTTP server startup and servlet registration
- **`servlets/TopicDisplayer.java`**: Handles `/publish` GET requests
- **`servlets/ConfLoader.java`**: Handles `/upload` POST requests  
- **`servlets/HtmlLoader.java`**: Serves static HTML files from `/app/`

### 📊 Core System (`project_biu/graph/`)
- **Topic Management**: Publish-subscribe messaging system
- **Agent System**: Computational agents that process messages
- **Graph Generation**: Creates visual representations of computation flows

## 🎯 Features

### 📁 Configuration Upload
- Upload `.conf` files through the web interface
- Automatic parsing and agent creation
- Real-time graph generation

### 📤 Topic Messaging  
- Send messages to topics via web forms
- GET requests to `/publish?topic=<name>&message=<text>`
- Results displayed in HTML tables

### 🔗 Graph Visualization
- Interactive directed graph display
- Color-coded nodes by type:
  - 🟦 **Input nodes** (variables)
  - 🟥 **Operator nodes** (+, -, ×, ÷)
  - 🟩 **Output nodes** (results)
- Click nodes for detailed information
- Example graphs: `(A + B) × (A - B)`, complex expressions

### 🌐 Static File Serving
- Secure file serving from designated directories
- Support for HTML, CSS, JS, images
- Path traversal protection

## 🏗️ Architecture

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Left Panel    │  │  Center Panel   │  │  Right Panel    │
│  (form.html)    │  │  (graph.html)   │  │  (temp.html)    │
│                 │  │                 │  │                 │
│ • Upload Form   │  │ • Graph Display │  │ • System Status │
│ • Message Form  │  │ • Interactions  │  │ • Additional    │
│ • Results Table │  │ • Examples      │  │   Content       │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
                    ┌─────────────────┐
                    │  HTTP Server    │
                    │  (Port 8080)    │
                    │                 │
                    │ • /publish      │
                    │ • /upload       │
                    │ • /app/         │
                    └─────────────────┘
                               │
               ┌───────────────┼───────────────┐
               │               │               │
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │ TopicDisplayer  │ │   ConfLoader    │ │   HtmlLoader    │
    │    Servlet      │ │    Servlet      │ │    Servlet      │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
               │               │               │
               └───────────────┼───────────────┘
                               │
                    ┌─────────────────┐
                    │ Topic Manager   │
                    │ Graph System    │
                    │ Agent Network   │
                    └─────────────────┘
```

## 📝 Configuration Format

Configuration files define agents and their connections:

```
graph.PlusAgent
input1,input2
output
graph.BinOpAgent  
output,constant
final_result
```

Format: 
- Line 1: Agent class name
- Line 2: Subscription topics (comma-separated)
- Line 3: Publication topics (comma-separated)

## 🔌 API Endpoints

### GET `/publish`
Publishes a message to a topic.
- **Parameters**: `topic`, `message`
- **Response**: HTML table with publication results

### POST `/upload`  
Uploads and processes configuration files.
- **Body**: Multipart form data with file
- **Response**: HTML with graph information and statistics

### GET `/app/{filename}`
Serves static HTML files.
- **Path**: Filename within `html_files/` directory
- **Response**: File content or 404 error

## 🛡️ Security Features

- **Path traversal protection** in file serving
- **Input validation** for all form parameters  
- **HTML escaping** to prevent XSS attacks
- **Directory restriction** for uploaded files

## 🎨 UI Features

- **Responsive design** that works on desktop and mobile
- **Modern styling** with gradients and animations
- **Interactive elements** with hover effects
- **Real-time updates** between panels
- **Professional error handling** with styled error pages

## 🔧 Development

### Adding New Servlets
1. Implement the `Servlet` interface
2. Register in `Main.java` using `server.addServlet()`
3. Handle `RequestInfo` and write to `OutputStream`

### Extending Graph Visualization
- Modify `graph.html` JavaScript functions
- Add new node types and edge styles
- Integrate with server-side graph data

### Custom Agent Types
- Extend the `Agent` class
- Implement topic subscription/publication logic
- Add to configuration files for automatic instantiation

## 📊 Example Usage

1. **Start the server**: `./start_server.sh`
2. **Open browser**: http://localhost:8080/app/index.html  
3. **Upload config**: Use the upload form in left panel
4. **View graph**: Computation graph appears in center panel
5. **Send messages**: Use topic messaging form
6. **Interact**: Click nodes to see details, try different examples

## 🔍 Troubleshooting

- **Port 8080 in use**: Change port in `Main.java`
- **Compilation errors**: Ensure all `.java` files are in correct packages
- **File upload fails**: Check file permissions in upload directory
- **Graph not showing**: Verify configuration file format

## 📈 Future Enhancements

- **Real-time graph updates** from server data
- **WebSocket support** for live messaging
- **Graph export** to various formats
- **Multi-user support** with session management
- **Advanced graph layouts** and algorithms
