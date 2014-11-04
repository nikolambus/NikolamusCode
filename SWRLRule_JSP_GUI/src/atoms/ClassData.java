package atoms;

public class ClassData {
	
	String classname;
    String classvar;

    public void setClassname( String value )
    {
        classname = value;
    }

    public void setClassvar( String value )
    {
        classvar = value;
    }

    
    public String getClassname() { return classname; }

    public String getClassvar() { return classvar; }
    
    public void reset() {
    	classname="";
    	classvar="";
    }
    
}
