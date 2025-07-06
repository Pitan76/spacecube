# Space Cube
![image](https://github.com/Pitan76/spacecube/assets/58260965/b81ecbc7-5682-4ceb-921e-777ad277702d)

Links: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spacecube), [Modrinth](https://modrinth.com/mod/spacecube)

Space Cube is a mod that creates space in another dimension

Fabric版のCompact Machinesが欲しいなーと思い、1から作って再現してみました。
I thought I'd like a Fabric version of Compact Machines, so I made one from scratch and tried to reproduce it.

## Dependency
https://maven.pitan76.net/v/#net/pitan76/itemalchemy/ <br />
Check the latest version on the above link.

- gradle.properties
```properties
# check these on https://maven.pitan76.net/v/#net/pitan76/
mcpitanlib_version=1.20.4:3.3.7
itemalchemy_version=1.1.3
```

----

- build.gradle
```groovy
repositories {
    maven { url "https://maven.pitan76.net/" }
}

dependencies {
    modImplementation "net.pitan76:mcpitanlib-fabric-${project.mcpitanlib_version}"
    modImplementation "net.pitan76.spacecube-fabric:0.9.13"
}
```