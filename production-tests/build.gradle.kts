plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
}
tasks {
    dockerBuildContext {
        from(projectDir) {

            include("conftest.py")
            include("pytest.ini")
            include("test_api.py")
            include("test_browser.py")
            include("requirements.smoke.txt")
        }
    }

    dockerBuild {
        imageName.set("production-tests")
    }
}