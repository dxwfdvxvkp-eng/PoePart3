# QuickChat – Programming 1A POE

## NetBeans
Open the `PoePart1_Corrected` folder as a NetBeans Ant project. Use JDK 21. Run the main class `poepart1.PoePart1`.

## Tests
The automated test class is `test/poepart1/QuickChatTests.java`. Run it as a Java main class, or run the included GitHub Actions workflow on a push or pull request. It has no external test dependencies. Local command:

```sh
mkdir -p build/classes
javac -d build/classes src/poepart1/*.java test/poepart1/*.java
java -cp build/classes poepart1.QuickChatTests
```

## Features
Part 1: username/password/SA cellphone validation, registration, login, welcome message.
Part 2: menu, generated 10-digit IDs, message hashes, message length/recipient validation, send/discard/store decisions, JSON writing.
Part 3: aligned arrays for message IDs, hashes, recipients, statuses and text; separate sent/stored/disregarded collections; stored-message menu; longest stored message; searches by ID and recipient; delete by hash; JSON reading; full report; automated regression tests.

## Notes
- Message data is saved to `messages.json` in the working directory. Do not put private real-world data in the repository.
- The Part 3 sample includes a developer entry `0838884567` where the message ID test references that value. This is inconsistent with the 10-digit generated-ID requirement. The implementation uses generated 10-digit IDs, not a cellphone number as an ID.
- The Part 3 sample says search recipient `+27838884567` and lists two stored messages. The implementation searches the actual recipient field.
- `QuickChatTests` is a self-contained automated Java test harness, rather than TestNG/JUnit. It runs in GitHub Actions. If your lecturer requires TestNG `assertEquals` tests specifically, convert/add those before submission.

## Improvements in this revision
- JSON is now parsed by `JsonMessageParser`, a strict parser for the string-field JSON schema used by this project, rather than regex extraction. It supports escaped quotation marks, backslashes, control escapes and Unicode escapes. This is not a general-purpose JSON parser for arbitrary nested values.
- Each message stores its own sender and preserves the sender when reloaded. Sender arrays stay aligned after deleting by hash.
- `junit/poepart1/QuickChatJUnitTest.java` adds actual JUnit 5 test methods using `assertEquals`, `assertTrue`, and `assertThrows`.
- GitHub Actions downloads JUnit Platform Console Standalone and executes the JUnit suite, as well as the dependency-free regression harness.
- The `junit` directory is kept separate from NetBeans' default `test` source root so the project still opens and compiles without downloading libraries. For IDE test execution, add JUnit 5 to the project and register the `junit` directory as a test source root; GitHub Actions is configured automatically.
- Do not claim an official mark from automated tests; assess against your lecturer's rubric and confirm the GitHub Actions workflow in your own repository.

## Final rubric-alignment checks (2026)
- The exact five Part 3 sample message bodies, flags, and available recipient values are covered in **both** `test/poepart1/QuickChatTests.java` and `junit/poepart1/QuickChatJUnitTest.java`.
- The fourth sample labels `0838884567` as **Developer** rather than Recipient; the fixture uses the international recipient `+27838884567` used for the other matching messages. The rubric's separate message-ID search test also uses `0838884567`, which conflicts with its requirement for generated 10-digit message IDs. Tests therefore use a valid 10-digit message ID for message 4. Confirm these contradictions with your lecturer.
- **Do not prepopulate real users' messages at every startup.** The application loads persisted user records; the exact five prescribed examples are exercised in isolated tests. This avoids contaminating the user's stored messages.
- JSON loading validates all incoming records before adding any, and preserves message numbering across restarts.
- The included JUnit 5 suite is executed by GitHub Actions when pushed to a repository. The workflow itself must be run on your GitHub account to confirm a successful CI run; a ZIP alone cannot prove this.
- No automatic test or reviewer can guarantee a lecturer will award 300/300. The marking rubric also assesses GitHub use, code quality and the complete interactive workflow.
