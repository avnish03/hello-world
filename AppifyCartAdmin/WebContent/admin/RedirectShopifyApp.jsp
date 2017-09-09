<html>
<body>
<%@page import="java.util.* " %>
<%

Base64.Decoder decoder = Base64.getDecoder();

String currentUser = request.getParameter("currentUser");
byte[] decodedByteArray = decoder.decode(currentUser);
currentUser = new String(decodedByteArray);

String loginStatus = request.getParameter("loginstatus");
decodedByteArray = decoder.decode(loginStatus);
loginStatus = new String(decodedByteArray);

String loginToken = request.getParameter("loginToken");
decodedByteArray = decoder.decode(loginToken);
loginToken = new String(decodedByteArray);

String currentShop = request.getParameter("currentShop");
decodedByteArray = decoder.decode(currentShop);
currentShop = new String(decodedByteArray);

String redirectTo = request.getParameter("redirectTo");
decodedByteArray = decoder.decode(redirectTo);
redirectTo = new String(decodedByteArray);
redirectTo = (redirectTo.equalsIgnoreCase("pricing") )?"AppifyPricing.html":"DevelopmentAppCreator.html";

String freeTrialStatus = request.getParameter("freeTrialStatus");

//out.println("currentuser"+currentUser);
//out.println("loginStatus"+loginStatus);
//out.println("loginToken"+loginToken);
    if (loginToken == "failed") {
        out.println("There is some error while login, Please login on Website");
    } else {
        %>
          <script type="text/javascript">
                sessionStorage.setItem('ls.accesstoken','"<%=loginToken%>"');
                sessionStorage.setItem('ls.currentuser','"<%=currentUser%>"');
                sessionStorage.setItem('ls.signupmethod','"shopifyapp"');
                sessionStorage.setItem('ls.shopifyshop','"<%=currentShop%>"');
                sessionStorage.setItem('ls.freeTrialStatus','"<%=freeTrialStatus%>"');
                window.location="<%=redirectTo%>";
          </script>

	<%
//                window.location="DevelopmentAppCreator.html";
//        String redirectURL = "DevelopmentAppCreator.html";
//        response.sendRedirect(redirectURL);
    }
%>


</body>
</html
