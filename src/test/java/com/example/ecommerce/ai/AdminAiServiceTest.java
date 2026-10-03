// TEST IMPLEMENTATION NOTES ONLY. This file intentionally contains no test code.
// Next AI: mock GeminiService, SqlValidationService, and ReadOnlySqlService. Verify strict call order and that rejected SQL never reaches execution.
// Cover empty results/no-data, unsupported and ambiguous questions, provider/database errors, bounded rows, and summaries grounded only in query results.
// Add mocked Gemini HTTP contract tests; never require a real key or external network.
