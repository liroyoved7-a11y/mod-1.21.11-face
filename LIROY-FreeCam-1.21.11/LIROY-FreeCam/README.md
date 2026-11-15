# LIROY FreeCam — Fabric 1.21.11

A client-side Fabric mod project.

## Controls
- Right Shift: toggle FreeCam
- WASD: move the camera
- Space: move up
- Left Ctrl: move down
- Left Alt: faster camera

## Requirements
- Minecraft Java Edition 1.21.11
- Fabric Loader
- Fabric API for 1.21.11
- Java 21 for building

## Build the JAR
1. Upload this whole project to a new GitHub repository.
2. On your computer, install Git and Java 21 if you do not already have them.
3. Clone the repository:
   `git clone YOUR_REPOSITORY_URL`
4. Open a terminal in the project folder and run:
   - Windows: `gradle build`
   - Linux/macOS: `gradle build`
5. The mod JAR will be in `build/libs/LIROY-FreeCam-1.0.0.jar`.
6. Put the JAR in `.minecraft/mods` alongside Fabric API.

## Important first-version note
This is an early client-side camera prototype. It changes the rendered camera position locally; it does not move the camera position on the server. Mouse rotation currently follows the player's view, so turning the camera also turns the player's view direction. Use it in single-player or servers where the server rules allow client-side FreeCam mods. Do not use it to bypass server rules.
