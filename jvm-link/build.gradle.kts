plugins {
    idea
    com.seeq.build.base
}

idea {
    module {
        excludeDirs = excludeDirs + (file("image"))
        excludeDirs = excludeDirs + (file("log"))
    }
}