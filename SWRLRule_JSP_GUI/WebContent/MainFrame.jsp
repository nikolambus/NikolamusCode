<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Main with frames</title>
</head>

<frameset rows="50%,50%">
  <frame src="Body.jsp" name="Body">
  <frame src="Head.jsp" name="Head">
  <noframes>
    <body>
    	<p><a href="Body.jsp">Body</a> <a href="Head.jsp">Head</a></p>
    </body>
  </noframes>
</frameset>
</html>