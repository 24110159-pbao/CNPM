// IMPLEMENTATION NOTES ONLY. This file intentionally contains no JavaScript implementation.
// Next AI: implement vanilla JS for the Thymeleaf manager chat fragment; POST JSON question to /manager/ai/chat.
// Read response defensively, show loading/errors, disable duplicate submit, cap question length, and keep history in browser memory only.
// Use textContent/createTextNode for all messages; never inject user/model content with innerHTML.
// Never store or send Gemini/API/database credentials from the browser. Do not persist chats to a new backend table.
