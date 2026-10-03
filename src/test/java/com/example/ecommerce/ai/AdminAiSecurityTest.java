// TEST IMPLEMENTATION NOTES ONLY. This file intentionally contains no test code.
// Next AI: use Spring Security/MockMvc tests for /manager/ai/**: anonymous is redirected/denied, ROLE_USER receives 403, ROLE_MANAGER can reach the chat endpoint.
// Verify no broader permitAll matcher bypasses the manager restriction. Mock AdminAiService; do not call Gemini or the real database.
