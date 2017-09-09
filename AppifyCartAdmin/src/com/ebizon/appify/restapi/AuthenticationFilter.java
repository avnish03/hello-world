package com.ebizon.appify.restapi;

import com.ebizon.appify.utils.Authorization;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;


/**
 * Created by manish on 26/12/16.
 */

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {
    //TODO(Manish) : This is temp solution, fix it with annotation
    private static final String[] unsecuredEndPoints = {"login.json", "register.json", "forgotPassword.json", "appdownload.json", "resetPassword", "return.json", "iOSBuildAcknowledge.json",  "formRegister.json", "RedirectShopifyApp.jsp"};
    @Override
    public ContainerRequest filter(ContainerRequest requestContext) {

        String uri = requestContext.getRequestUri().toString();
        System.out.println(uri);

        //TODO(Manish) : fix it with annotation
        for (String endPoint : unsecuredEndPoints){
            String urlEndPoint = "/admin/" + endPoint;
            String urlShopifyAppInstall = "/shopifyapp/" ;
            if (uri.contains(urlEndPoint) || uri.contains(urlShopifyAppInstall)) {
                System.out.println(String.format("Toeken check not needed for url %s", uri));
                return requestContext;
            }
        }


        String authorizationHeader = requestContext.getHeaderValue(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Authorization failed either header is null or not have bearer");
            throw new WebApplicationException(getFailedResponse());
        }

        String token    = authorizationHeader.substring("Bearer".length()).trim();

        boolean isValid = validateToken(token);

        if (!isValid) {
            System.out.println(String.format("Authorization failed token %s is not valid", token));
            throw new WebApplicationException(getFailedResponse());
        }

        return requestContext;
    }

    private Response getFailedResponse() {
        String response = "Authorization failed";
        return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
    }

    private boolean validateToken(String token){
        return Authorization.isValid(token);
    }
}
