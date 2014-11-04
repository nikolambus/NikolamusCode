package atoms;

public class PropertyData {
	
	String propertyname;
    String propertyvar1;
    String propertyvar2;


    public void setPropertyname( String value )
    {
        propertyname = value;
    }

    public void setPropertyvar1( String value )
    {
        propertyvar1 = value;
    }
    
    public void setPropertyvar2( String value )
    {
        propertyvar2 = value;
    }

    
    public String getPropertyname() { return propertyname; }

    public String getPropertyvar1() { return propertyvar1; }
	
    public String getPropertyvar2() { return propertyvar2; }
    
    public void reset() {
    	propertyname="";
    	propertyvar1="";
    	propertyvar2="";
    }

}
