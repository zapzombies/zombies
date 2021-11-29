# zombies (Readme is under development)
Recreation of the Hypixel Arcade minigame Zombies. Seeks to fix bugs and add features missing from the original game.

## Setup
What you will need
- [JDK 16](https://www.oracle.com/java/technologies/javase-jdk16-downloads.html)
- [Gradle](https://gradle.org/install/) version 7 or higher (if you are using gradle bundled with IntelliJ, make sure to update your Environment Variable)
- IDE of choice, [IntelliJ](https://www.jetbrains.com/idea/) is recomended

Steps
- Clone this repository
- If you are using IntelliJ, Make sure you reload gradle changes by pressing `Ctrl + Shift + O`
- Set Project JDK to JDK 16 *Usually this happens automatically but IntelliJ is high sometimes*: Press `Ctrl + Shift + Alt + S`, under `Project SDK` select `openjdk-16` (this might not be the same depending on the version you installed)
- To build the project run `gradle build` or using the `build` Run/Debug Configuration included in IntelliJ
- To run unit tests run  `gradle test` or using the `test` Run/Debug Configuration included in IntelliJ

## IntelliJ Run Configurations
There are several  run configurations that help you with setting up environment / debugging. The most important are:
*(Not everything is included, I am lazy to write multiple readme)*
- Run development server: Create a localhost minecraft server for you to test out your plugin locally
- Build: build the project - for project isn't a plugin
- Publish to Maven local: Create a local version of a project. This is helpful when you need to quickly testing more than 1 project that depends on each other
- Invalidate PAT: If there is a problem when resolving dependencies, running this configuraiton might help

Not so important one
- Test: test the project
- Setup Localhost: Create a localhost server under `projectDir/run/server-1`
- Setup Maven local: Setup maven packages that doesn't host publicaly (ex: NMS)
- Setup PAT: Prompt user to provide Personal Access Token

## ~~Interproject manual testing~~ (No longer requried with `qs`)
Since we split our monolithic project into multiple smaller project, editing 2 projects at the same time will be a bit harder, here how we can simplify it a bit
Suppose you have a project `A` depends on project `B`:
- Making changes to `A`
- `A`: Instead of running `Build` or `Run development server`, run `Publish to Mavel local`
- In project `B` replace the public version with your. The version should be `0.0.0-SNAPSHOT` for local version. I suggest type both version and comment out the public one for "quick switch"
```kt
// Uncomment to use local maven version - help local testing faster
dependencies {
  // bukkitPlugin("io.github.zap:zap-party:1.0.0")
  bukkitPlugin("io.github.zap:zap-party:0.0.0-SNAPSHOT")
}
```
- Make sure you revert to the public version before making pull request


## Guidelines
### Code conventions
We are not too strict on our guidelines. Usage of JetBrains `@NotNull` and `@Nullable` annotations are encouraged. Your code should adhere to typical Java conventions for the most part (use your own judgement). Commits to the unstable branch are regularly merged and should only be for quick small fixes. All other major feature changes should be submitted as Pull Requests to the main branch. Please review other contributors' Pull Requests. Ideally, multiple people should review large pull requests before merging.

### Branches
- `master` branch contains code uses for production and should not be commited directly. Any release tag will create a Github Package automatically
- `staging` branch contains code for general testing, only hotfix/patches should be directly commited here. Push to this branch will create a snapshot version automatically
- To work on a new feature, created a new branch and open a pull request to merge to `staging`
