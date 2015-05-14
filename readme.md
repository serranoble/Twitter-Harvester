# About the project
This project is a Maven project done using Eclipse Luna and Oracle Java 1.8.

# Jar generation
To do this successfully, please execute the Maven build command using "clean compile assembly:single" as goals

# How to use it
After configure both properties files, one for twitter and the other for couchdb, just type:
java -jar twitter-harvester-<version_info>.jar

The log directory will be automatically created handling a rolling appender file system with 10 files, 50 MB each. Log configuration is available inside the jar file in the resources folder.

# Other considerations
- The worker code is a mess... but I think that is clear if you read previously the HBC Twitter readme.
- Also, some logging was added. The configuration file is inside the resources folder, following Maven ideas...
- I code again the Harvester eliminating the keyboard interface. Hopefully, a Socket port will be added to support stats consultations in the future.

# Random Stuff
Please, you are free to change it if you want. Also, provide me some feedback if necessary.