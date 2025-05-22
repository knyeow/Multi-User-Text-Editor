# Multi-User Text Editor Protocol

## Protocol Overview
This document describes the custom protocol used for communication between the server and clients in the Multi-User Text Editor system.

## Message Format
Each message consists of a header and a body, separated by a newline character (\n).
The header contains the message type and length, while the body contains the actual data.

Format: `MESSAGE_TYPE|LENGTH\nBODY`

## Message Types

### Authentication Messages
1. LOGIN_REQUEST
   - Client -> Server
   - Body: username
   - Response: LOGIN_RESPONSE

2. LOGIN_RESPONSE
   - Server -> Client
   - Body: "SUCCESS" or "FAILURE:reason"

### Document Management Messages
1. CREATE_DOCUMENT
   - Client -> Server
   - Body: document_name
   - Response: DOCUMENT_CREATED

2. DOCUMENT_CREATED
   - Server -> Client
   - Body: document_id

3. LIST_DOCUMENTS
   - Client -> Server
   - Body: empty
   - Response: DOCUMENT_LIST

4. DOCUMENT_LIST
   - Server -> Client
   - Body: JSON array of document information

5. OPEN_DOCUMENT
   - Client -> Server
   - Body: document_id
   - Response: DOCUMENT_CONTENT

6. DOCUMENT_CONTENT
   - Server -> Client
   - Body: document content

### Text Editing Messages
1. TEXT_UPDATE
   - Client -> Server
   - Body: JSON object containing:
     - document_id
     - position
     - text
     - operation_type (INSERT/DELETE)

2. TEXT_UPDATE_BROADCAST
   - Server -> All Clients
   - Body: Same as TEXT_UPDATE

### Error Messages
1. ERROR
   - Server -> Client
   - Body: error message

## State Machine
```
[Client]                    [Server]
   |                           |
   |-- LOGIN_REQUEST -------->|
   |<-- LOGIN_RESPONSE -------|
   |                           |
   |-- LIST_DOCUMENTS ------->|
   |<-- DOCUMENT_LIST --------|
   |                           |
   |-- OPEN_DOCUMENT -------->|
   |<-- DOCUMENT_CONTENT -----|
   |                           |
   |-- TEXT_UPDATE ---------->|
   |<-- TEXT_UPDATE_BROADCAST-|
   |                           |
```

## Error Handling
- All messages must be acknowledged
- Timeout after 5 seconds for any operation
- Automatic reconnection on connection loss
- Conflict resolution using server timestamp 