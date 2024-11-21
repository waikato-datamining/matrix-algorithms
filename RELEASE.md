How to make a release
=====================

* Switch to Java 11

* Run the following command to deploy the artifact:

  ```
  mvn release:clean release:prepare release:perform
  ```

* Push all changes

* Log into https://oss.sonatype.org/ and publish artifacts (close/release)

* Update Maven artficat version in [README.md](README.md#maven)
