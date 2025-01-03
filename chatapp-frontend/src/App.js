import React, { useState, useEffect } from "react";

const ChatApp = () => {
  const [username, setUsername] = useState(""); // Capture input text
  const [loggedIn, setLoggedIn] = useState(false); // Track login state
  const [socket, setSocket] = useState(null); // WebSocket connection
  const [users, setUsers] = useState(["Alice", "Bob", "Charlie"]); // List of users
  const [messages, setMessages] = useState({}); // Store messages for each user
  const [selectedUser, setSelectedUser] = useState(null);
  const [message, setMessage] = useState("");

  const handleLogin = (e) => {
    e.preventDefault();
    if (username) {
      setLoggedIn(true); // Set logged in to true when username is provided
    }
  };

  // WebSocket connection will be established once the user logs in
  useEffect(() => {
    if (loggedIn) {
      const ws = new WebSocket(`ws://localhost:8080/chat/${username}`);

      ws.onopen = () => {
        console.log("WebSocket connection established for", username);
      };

      ws.onmessage = (e) => {
        const { from, message: incomingMessage } = JSON.parse(e.data);

        // Update the messages state for the relevant user
        setMessages((prevMessages) => ({
          ...prevMessages,
          [from]: prevMessages[from]
            ? [...prevMessages[from], incomingMessage]
            : [incomingMessage],
        }));
      };

      ws.onerror = (e) => {
        console.log("WebSocket error:", e);
      };

      ws.onclose = () => {
        console.log("WebSocket connection closed.");
      };

      setSocket(ws);

      return () => {
        // Cleanup the WebSocket when the component unmounts or when the username changes
        ws.close();
      };
    }
  }, [loggedIn]); // This effect runs when `loggedIn` or `username` changes

  const handleSelectUser = (targetUsername) => {
    setSelectedUser(targetUsername);
  };

  const handleSendMessage = () => {
    if (socket && message && selectedUser) {
      const msgToSend = `${selectedUser}:${message}`;

      // Send the message in the format [toUsername]:[message]
      socket.send(JSON.stringify({ to: selectedUser, message }));

      // Update messages state for the selected user
      setMessages((prevMessages) => {
        return {
          ...prevMessages,
          [selectedUser]: [...(prevMessages[selectedUser] || []), message],
        };
      });

      // Clear message input after sending
      setMessage("");
    }
  };

  return (
    <div>
      {!loggedIn ? (
        <form onSubmit={handleLogin}>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter your username"
          />
          <button type="submit">Login</button>
        </form>
      ) : (
        <div>
          <h2>Welcome, {username}!</h2>
          <ul>
            {users.map((user) => (
              <button key={user} onClick={() => handleSelectUser(user)}>
                {user}
              </button>
            ))}
          </ul>

          {selectedUser && (
            <div>
              <h3>Chat with {selectedUser}</h3>
              <div>
                {messages[selectedUser] &&
                  messages[selectedUser].map((msg, index) => (
                    <p key={index}>{msg}</p>
                  ))}
              </div>
              <input
                type="text"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Type a message"
              />
              <button onClick={handleSendMessage}>Send</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ChatApp;
