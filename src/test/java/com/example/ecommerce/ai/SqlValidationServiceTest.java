// TEST IMPLEMENTATION NOTES ONLY. This file intentionally contains no test code.
// Next AI: test safe SELECT acceptance and rejection of all write/DDL statements, multi-statement SQL, comments/obfuscation, CTE/UNION policy, INTO OUTFILE, cross-schema access, unknown/sensitive columns, dangerous functions, locking reads, and unbounded list queries.
// For every rejected SQL case, verify ReadOnlySqlService/executor was never invoked. Use parser tests; no real DB or Gemini required.
