package atoms;

public class BuiltinData {

	String builtinname;
	String builtinvar1;
	String builtinvar2;
	
	 public void setBuiltinname( String value )
	    {
	        builtinname = value;
	    }

	    public void setBuiltinvar1( String value )
	    {
	        builtinvar1 = value;
	    }
	    
	    public void setBuiltinvar2( String value )
	    {
	        builtinvar2 = value;
	    }

	    
	    public String getBuiltinname() { return builtinname; }

	    public String getBuiltinvar1() { return builtinvar1; }
		
	    public String getBuiltinvar2() { return builtinvar2; }

	    public void reset() {
	    	builtinname="";
	    	builtinvar1="";
	    	builtinvar2="";
	    }
}
