# Multi-User Text Editor

A real-time collaborative text editor that allows multiple users to edit documents simultaneously, similar to Google Docs.

## Features
- Real-time text editing collaboration
- Multiple document support
- User authentication
- Automatic server-side saving
- Cut, Copy, Paste functionality
- Simple and intuitive GUI

## Technical Details
- Built with Java
- Uses TCP for reliable communication
- Custom protocol for client-server communication
- Swing-based GUI

## Project Structure
- `src/`
  - `server/` - Server-side code
  - `client/` - Client-side code
  - `common/` - Shared code and protocol definitions
  - `gui/` - GUI components

## How to Run
1. Start the server:
   ```
   java -cp target/multi-user-text-editor-1.0-SNAPSHOT-jar-with-dependencies.jar server.Server   
   ```
2. Start the client:
   ```
   java -cp target/multi-user-text-editor-1.0-SNAPSHOT-jar-with-dependencies.jar client.EditorGUI
   ```

## Protocol Documentation
The communication protocol is defined in the `Protocol.md` file. 