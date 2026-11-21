# QuickChat corrections and verification

Corrections include rubric-specific registration/login messages, international +27 number validation, alphabetic and numeric stored-message menu selection, recording disregarded messages, synchronized deletion from parallel arrays, sender information in reports, and duplicate message-ID rejection. GitHub Actions includes TestNG dependencies.

The five prescribed Part 3 sample messages are used in MessageNGTest.java (four in setup and the fifth in the recipient-search test). They are not inserted into a real user's data at startup.

## Verification
- Java application source compiled using javac.
- Interactive registration/login/quit smoke test completed.
- TestNG execution and GitHub Actions execution have NOT been independently verified here. Run the GitHub workflow before submitting.

## Remaining limitations
- The stored-message JSON loader uses a limited regular-expression-based reader rather than a full standards-compliant JSON parser. It is intended for the application's own generated file format.
- The rubric is internally inconsistent about some message-ID and cellphone test data. For instance message 4 uses 0838884567 in a message-ID test.
- Some existing tests may need adjustment if lecturer expectations differ on wording and exact formatting.
- No 300/300 mark is guaranteed.
