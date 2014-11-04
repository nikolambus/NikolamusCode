<jsp:useBean id="bui" class="atoms.BuiltinData" scope="session"/>
<jsp:setProperty name="bui" property="*"/> 
<jsp:useBean id="user" class="atoms.UserData" scope="session"/>
<jsp:setProperty name="user" property="*"/> 
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>building a SWRL builtin</title>
</head>
<body>
<p>
<p>

Please enter the builtin data.
<FORM METHOD=POST>
What's your builtin name? <INPUT TYPE=TEXT NAME=builtinname SIZE=20><BR>
What's your builtin first var? <INPUT TYPE=TEXT NAME=builtinvar1 SIZE=5><BR>
What's your builtin second var? <INPUT TYPE=TEXT NAME=builtinvar2 SIZE=5><BR>
<P><INPUT TYPE=SUBMIT>
</FORM>

<p>
Your Builtin: <%=bui.getBuiltinname()%> <%=bui.getBuiltinvar1()%> <%=bui.getBuiltinvar2()%> <br>
<p>

<% user.setRuleProperty(bui.getBuiltinname(), bui.getBuiltinvar1(), bui.getBuiltinvar2()); %>
<% bui.reset(); %>
<% user.reset(); %>

<a href="Body.jsp"> continue </a>

</body>
</html>