###### Karl-John Chow on
# Adding the MED_library to your Android project 
### in Android Studio

## Purpose

This repository is made so we can have a library which we can use anytime for any project where we always keep our core architecture & functions inside. Please use this with all the projects we make.

## 1. Terminal
First open your terminal and do a git pull on (depending on if you use https or ssh auth):
* https://gitlab.com/imazeapp/med-library.git
* git@gitlab.com:imazeapp/med-library.git

Make sure that you have the right access rights. If you don’t, mail/slack me

## 2. Location
Put it into the same folder as the folder where your project is located so for example:
1. Documents/Code
⋅⋅* nevo 
⋅⋅* MED_library

## 3. settings.graddle
Go to your settings.graddle file in your project (in this example, nevo). In this case nevo. Add the following 2 lines:
```graddle
include ':library'
project(':library').projectDir = new File(settingsDir, '../../../MED_library/library')
```

Make sure that you navigate to Documents/Code with the ../../. 

## 4. build.graddle 
Go to your build.graddle from the module (which means, not the build.graddle from your project). This build.graddle is the file also the other dependencies are. Add the following dependency in dependencies:
```graddle
compile project(‘:library')
```

## 5. manifest tools
Go to your manifest and at in your manifest tag:
```xml
xmlns:tools=“http://schemas.android.com/tools"
```

## 6. manifest dependency
Go to your application tag and add:
```xml
tools:replace=“android:theme"
```

And you are done!

## Contact

If you have any questions please, send me a mail: karl@med-corp.net