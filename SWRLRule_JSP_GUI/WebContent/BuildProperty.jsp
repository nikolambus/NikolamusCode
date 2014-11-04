<jsp:useBean id="prop" class="atoms.PropertyData" scope="session"/>
<jsp:setProperty name="prop" property="*"/> 
<jsp:useBean id="user" class="atoms.UserData" scope="session"/>
<jsp:setProperty name="user" property="*"/> 
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>building a SWRL property</title>
</head>
<body>
<p>
<p>

Please enter the property data.
<FORM METHOD=POST>
What's your property name? <INPUT TYPE=TEXT NAME=propertyname SIZE=20><BR>
What's your property first var? <INPUT TYPE=TEXT NAME=propertyvar1 SIZE=5><BR>
What's your property second var? <INPUT TYPE=TEXT NAME=propertyvar2 SIZE=5><BR>
<P><INPUT TYPE=SUBMIT>
</FORM>

<p>
Your Property: <%=prop.getPropertyname()%> <%=prop.getPropertyvar1()%> <%=prop.getPropertyvar2()%> <br>
<p>

<% user.setRuleProperty(prop.getPropertyname(), prop.getPropertyvar1(), prop.getPropertyvar2()); %>
<% prop.reset(); %>
<% user.reset(); %>

<a href="Body.jsp"> continue </a>

</body>
</html>