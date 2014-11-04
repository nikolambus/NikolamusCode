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
<title>Main body menu</title>
</head>
<body bgcolor="#E0C0FF">

Let us create a <b>body</b>!<br> 
What would you like to add?<br>

<form method=get>
  <p>
    <input type="checkbox" name="choice" value="class"> Class<br>
    <input type="checkbox" name="choice" value="property"> Property<br>
    <input type="checkbox" name="choice" value="builtin"> Builtin<br>
    <input type="checkbox" name="choice" value="finish"> Finish<br>
    <input type=submit value="Submit">
  </p>
</form>

<% 
if (user.choice.equals("class")) { %> 
    <jsp:forward page="BuildClass.jsp"></jsp:forward>
<% } %>	

<% if (user.choice.equals("property")) { %>
    <jsp:forward page="BuildProperty.jsp"></jsp:forward>
<% } %>	

<% if (user.choice.equals("builtin")) { %>
	<jsp:forward page="BuildBuiltin.jsp"></jsp:forward>
<% } %>	

<% if (user.choice.equals("finish")) { %>
	<jsp:forward page="Finish.jsp"></jsp:forward>	
<% } %>	

<p>

Your <b>body</b> till now: <br>

<%= user.getRule() %> <br>

<p>
	
Your variables till now: <br>

<%= user.getVarsBank() %>

</body>
</html>