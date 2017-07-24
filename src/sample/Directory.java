import java.io.File;

class Directory extentds File
{
    private boolean wasOpenned = false;
    
    public Directory(){
        super();
    }
    
    public boolean getWasOpenned(){
        return wasOpenned;
    }
    
    public void open(){
        wasOpenned = true;
    }
    
    @Override
    public String toString(){
        return getName();
    }
   
    public Directory[] listDirs(){
        open();
        return (Directory)super.listFiles(file -> file.isDirectory());
    }
}
