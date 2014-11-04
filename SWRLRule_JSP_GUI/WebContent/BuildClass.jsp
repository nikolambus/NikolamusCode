<jsp:useBean id="user" class="atoms.UserData" scope="session"/>
<jsp:setProperty name="user" property="*"/> 
<jsp:useBean id="cls" class="atoms.ClassData" scope="session"/>
<jsp:setProperty name="cls" property="*"/>  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>building a SWRL class</title>
</head>
<body>
<p>
<p>

Please enter the class data.
<p>
<FORM METHOD=POST>
What's your class name?  <select name=classname size="1">
    					 	<option>Patient</option>
      						<option>DRU</option>
      						<option>Biopsie</option>
      						<option>DiagnosePhase</option>
      						<option>AmbulanzPhase</option>
    					</select>
<br>    					
What's your class var? <select name=classvar size="1">
    					 	<option>pat</option>
      						<option>dru</option>
      						<option>biop</option>
      						<option>diag</option>
      						<option>a</option>
      						<option>b</option>
      						<option>c</option>
      						<option>d</option>
      						<option>e</option>
      						<option>f</option>
      						<option>g</option>
      						<option>h</option>
      						<option>i</option>
      						<option>j</option>
      						<option>k</option>
      						<option>l</option>
      						<option>m</option>
      						<option>n</option>
      						<option>o</option>
      						<option>p</option>
      						<option>r</option>
      						<option>s</option>
      						<option>t</option>
      						<option>u</option>
      						<option>v</option>
      						<option>w</option>
      						<option>x</option>
      						<option>y</option>
      						<option>z</option>
    					</select>
<P><INPUT TYPE=SUBMIT>
</FORM>

<p>
Your Class: <%=cls.getClassname()%> <%=cls.getClassvar()%> <br>
<p>

<% user.addToVarsBank(cls.getClassvar()); %>

<% user.setRuleClass(cls.getClassname(), cls.getClassvar()); %>
<% cls.reset(); %>
<% user.reset(); %>

<a href="Body.jsp"> continue </a>

</body>
</html>