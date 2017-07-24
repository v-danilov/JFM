import java.io.File;

class Directory extentds File
{
    private boolean wasOpenned = false;
    
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
    
    @Override
    public File[] listFiles(){
        open();
        return super.listFiles();
}
