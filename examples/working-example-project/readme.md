1. Clone the whole project.
2. Then run `../../gradlew build` command.
3. The above command will fail with some module not found error. For that we need to install some modules in venv. I've created the separeate task for it.
4. Run `../../gradlew pipInstallRequirements` command to get all missing dependencies.
5. After that run `../../gradlew build` command
6. Yo! You've successfully built the **`pygradle`** project
