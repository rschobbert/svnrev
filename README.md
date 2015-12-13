svnrev
======

subversion revision tool

A standalone java tool similar to the subwcrev tool shiped with Tortoise SVN. Just call it with
```
java -jar svnrev-all-1.0.6.jar
```
or
```
java -jar svnrev-all-1.0.6.jar [wcRoot [outputDir [outputFilename [templatePath]]]]
```


Optional program arguments are:
wcRoot = working copy root, default is '.' (the current directory)
outputDir = the directory where the output file is written, default is '.' (the current directory)
outputFilename = the filename used to write the output file, the default is <current-dir-name>.build.properties
templatePath = the template file path, possible variables in the template are: ${REV}, ${COMMIT_DATETIME}, ${BUILD_DATETIME}, ${LOCALLY_MODIFIED}
