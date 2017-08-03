# JFM
Java file manager. Sipmle application to browse file system for Unix and Windwos OS's.

# Features and possibilities
- Navigation throught filesystem presents with tree view
- Display folder content with split view
- Lazy load sub folders that can help integration in web systems
- Folders (open/close) and files icons
- Default file system operations (create, copy, move, rename, delete)

# Realized functions
**JFM supports next commands:**
- Create new file (with file name validation)
- Create new directory (with file name validation)
- Copy files and directories
- Move files and directories
- Rename files and directories
- Delete files and directories

# Description
The app contains split screen with file tree system on the one hand and content folder on the other.

**Tree view**

Tree view is a navigation element. It used to move throught directries and display subfolders. 
When folder downloads first time you will see downloading indicator (animated circle). It's emulated the web process.
You also will see downloading animattion next times, but it will be means 'updating' loaded earlier content.

**List view**

List view helps to see the folder content. It displays all subfolder and files stored in the folder. 
Also you can use basic function there like replace, remove, rename, create, copy.
Open context menu with double click in list area or on the element to use one of this funtion.
