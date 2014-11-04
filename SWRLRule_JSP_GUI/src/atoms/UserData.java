package atoms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UserData {
    public String choice = "";
    public String rule = "";
    List<String> varsBank = new ArrayList<String>();
   
    //Alternative
    //List<String> varsBank = new LinkedList<String>();

    public void setChoice( String value )
    {
        choice = value;
    }
    
    public String getChoice() { return choice; }
    
    public void setRuleClass (String name, String var)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var + ")^";
    }
    
    public void setRuleProperty (String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var1 + ",?" + var2 + ")^";
    }
    
    public void setRuleBuiltin (String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var1 + ",?" + var2 + ")^";
    }
    
    public String getRule ()
    {
        return rule;
    }
    
    public void addToVarsBank(String var) {
    	if ((var != "") && (var != null))
    		varsBank.add(var);
    }
    
    public String getVarsBank() {
    	if (varsBank == null)
    		return "";
    	else
    		return varsBank.toString();
    }
    
    public void reset() {
    	choice = "";
    }

}
