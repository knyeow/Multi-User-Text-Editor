# Collaborative Text Editor Protocol Documentation

## Overview
This document describes the communication protocol used between the server and clients in the collaborative text editor. The protocol uses TCP for reliable communication and JSON for message formatting.

## Message Format
All messages follow this JSON structure:
```json
{
    "type": "MESSAGE_TYPE",
    "body": "message_content"
}
```

## Message Types

### Client to Server Messages

#### 1. LOGIN_REQUEST
- **Type**: `LOGIN_REQUEST`
- **Body**: Username string
- **Purpose**: Initial connection request from client
- **Example**:
```json
{
    "type": "LOGIN_REQUEST",
    "body": "john_doe"
}
```

#### 2. CREATE_DOCUMENT
- **Type**: `CREATE_DOCUMENT`
- **Body**: Document name string
- **Purpose**: Request to create a new document
- **Example**:
```json
{
    "type": "CREATE_DOCUMENT",
    "body": "Meeting Notes"
}
```

#### 3. OPEN_DOCUMENT
- **Type**: `OPEN_DOCUMENT`
- **Body**: Document ID string
- **Purpose**: Request to open an existing document
- **Example**:
```json
{
    "type": "OPEN_DOCUMENT",
    "body": "doc_123"
}
```

#### 4. TEXT_UPDATE
- **Type**: `TEXT_UPDATE`
- **Body**: JSON object containing:
  - `documentId`: string
  - `position`: integer
  - `text`: string
  - `isInsert`: boolean
- **Purpose**: Send text changes to server
- **Example**:
```json
{
    "type": "TEXT_UPDATE",
    "body": {
        "documentId": "doc_123",
        "position": 10,
        "text": "Hello",
        "isInsert": true
    }
}
```

#### 5. LIST_DOCUMENTS
- **Type**: `LIST_DOCUMENTS`
- **Body**: Empty string
- **Purpose**: Request list of available documents
- **Example**:
```json
{
    "type": "LIST_DOCUMENTS",
    "body": ""
}
```

### Server to Client Messages

#### 1. LOGIN_RESPONSE
- **Type**: `LOGIN_RESPONSE`
- **Body**: Success status string ("SUCCESS" or "FAILED")
- **Purpose**: Response to login request
- **Example**:
```json
{
    "type": "LOGIN_RESPONSE",
    "body": "SUCCESS"
}
```

#### 2. DOCUMENT_LIST
- **Type**: `DOCUMENT_LIST`
- **Body**: JSON array of document objects
- **Purpose**: Send list of available documents
- **Example**:
```json
{
    "type": "DOCUMENT_LIST",
    "body": [
        {
            "id": "doc_123",
            "name": "Meeting Notes",
            "owner": "john_doe"
        }
    ]
}
```

#### 3. DOCUMENT_CONTENT
- **Type**: `DOCUMENT_CONTENT`
- **Body**: Document content string
- **Purpose**: Send document content when opened
- **Example**:
```json
{
    "type": "DOCUMENT_CONTENT",
    "body": "Hello, this is the document content"
}
```

#### 4. TEXT_UPDATE_BROADCAST
- **Type**: `TEXT_UPDATE_BROADCAST`
- **Body**: Same as TEXT_UPDATE
- **Purpose**: Broadcast text changes to all clients
- **Example**:
```json
{
    "type": "TEXT_UPDATE_BROADCAST",
    "body": {
        "documentId": "doc_123",
        "position": 10,
        "text": "Hello",
        "isInsert": true
    }
}
```

#### 5. USER_LIST
- **Type**: `USER_LIST`
- **Body**: JSON array of usernames
- **Purpose**: Send list of connected users
- **Example**:
```json
{
    "type": "USER_LIST",
    "body": ["john_doe", "jane_smith"]
}
```

#### 6. ERROR
- **Type**: `ERROR`
- **Body**: Error message string
- **Purpose**: Send error notifications
- **Example**:
```json
{
    "type": "ERROR",
    "body": "Document not found"
}
```

## Communication Flow

### 1. Connection and Authentication
1. Client connects to server
2. Client sends `LOGIN_REQUEST`
3. Server responds with `LOGIN_RESPONSE`
4. Server broadcasts updated `USER_LIST`

### 2. Document Management
1. Client requests document list with `LIST_DOCUMENTS`
2. Server responds with `DOCUMENT_LIST`
3. Client can create new document with `CREATE_DOCUMENT`
4. Server updates and broadcasts new `DOCUMENT_LIST`

### 3. Document Editing
1. Client opens document with `OPEN_DOCUMENT`
2. Server responds with `DOCUMENT_CONTENT`
3. Client makes changes and sends `TEXT_UPDATE`
4. Server broadcasts changes as `TEXT_UPDATE_BROADCAST`

## Error Handling
- Invalid message types result in `ERROR` response
- Document operations on non-existent documents return `ERROR`
- Network disconnections are handled gracefully
- Server maintains document state and can recover from errors

## Security Considerations
- No authentication beyond username
- No encryption of messages
- No access control for documents
- Future versions should implement:
  - User authentication
  - Message encryption
  - Document access control
  - Session management

## Performance Considerations
- Text updates are sent as deltas (position + text)
- Server maintains document state
- Clients cache document content
- Auto-save feature prevents data loss
- Efficient broadcast mechanism for updates

## Future Enhancements
1. Add message versioning
2. Implement conflict resolution
3. Add document locking
4. Support for rich text
5. Add file attachments
6. Implement user roles and permissions
7. Add document sharing features
8. Support for offline editing 