package com.onlineshoppingsolutions.cache.service.echo;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class CacheExampleBackendResource {

    private static final Logger LOG = LoggerFactory.getLogger(CacheExampleBackendResource.class);
    private static final AtomicInteger REMAINING_WAIT_LOCKS = new AtomicInteger(100);

    public CacheExampleBackendResource() {
    }

    @Path("/force-cache/default")
    @GET
    @Timed
    /**
     * Force varnish to cache a non cacheable response
     * @return Response
     */
    public Response cacheForcedCache() {
        return getConfigurableCacheableResponse(0);
    }

    @Path("/force-no-cache/default")
    @GET
    @Timed
    /**
     * Force varnish not to cache a cacheable response
     * @return Response
     */
    public Response cacheForcedNoCache() {
        return getConfigurableCacheableResponse(100);
    }

    /**
     * Respect max header and path base params
     *
     * @param maxAge
     * @return Response
     */
    @Path("/max-age-header/{age}")
    @GET
    @Timed
    public Response maxAgePathParamResponseHeader(@PathParam("age") Integer maxAge) {
        return getConfigurableCacheableResponse(maxAge);
    }

    /**
     * Respect max header cache query params
     *
     * @param maxAge
     * @return Response
     */
    @Path("/max-age-header")
    @GET
    @Timed
    public Response maxAgeQueryParamResponseHeader(@QueryParam("age") Integer maxAge) {
        return getConfigurableCacheableResponse(maxAge);
    }

    /**
     * Cache various response codes
     *
     * @param responseStatusCode
     * @return Response
     */
    @Path("/status/{status}")
    @GET
    @Timed
    public Response statusCodePathParam(@PathParam("status") Integer responseStatusCode) {
        Response.ResponseBuilder responseBuilder = Response.status(responseStatusCode).entity(new ResponseMessage(false, responseStatusCode));
        return responseBuilder.build();
    }


    /**
     * Configure caching for slow responses
     *
     * @param responseTime
     * @return Response
     */
    @Path("/timing/{time}")
    @GET
    @Timed
    public Response okPathParamTiming(@PathParam("time") Integer responseTime) {
        sleepForNumberSeconds(responseTime);
        Response.ResponseBuilder responseBuilder = Response.ok(new ResponseMessage(false, Response.Status.OK.getStatusCode()));
        return responseBuilder.build();
    }

    /**
     * Configure caching for slow responses with configurable response code
     *
     * @param responseStatusCode
     * @param responseTime
     * @return Response
     */
    @Path("/status/{status}/timing/{time}")
    @GET
    @Timed
    public Response statusPathParamTiming(@PathParam("status") Integer responseStatusCode, @PathParam("time") Integer responseTime) {
        sleepForNumberSeconds(responseTime);
        Response.ResponseBuilder responseBuilder = Response.status(responseStatusCode).entity(new ResponseMessage(false, responseStatusCode));
        responseBuilder = responseBuilder.status(responseStatusCode);
        return responseBuilder.build();
    }

    /**
     * Configure caching for slow responses with configurable response code and max-age header
     *
     * @param maxAge
     * @param responseStatusCode
     * @param responseTime
     * @return
     */
    @Path("/max-age-header/{age}/status/{status}/timing/{time}")
    @GET
    @Timed
    public Response maxAgeStatusTimingPathParam(@PathParam("age") Integer maxAge, @PathParam("status") Integer responseStatusCode, @PathParam("time") Integer responseTime) {
        sleepForNumberSeconds(responseTime);
        Response.ResponseBuilder responseBuilder = Response.status(responseStatusCode)
                .header("Cache-Control", "max-age=" + maxAge)
                .entity(new ResponseMessage((maxAge > 0), responseStatusCode));
        responseBuilder = responseBuilder.status(responseStatusCode);
        return responseBuilder.build();
    }

    /**
     * Set a cookie with the given name and value and a caching header with a max-age.
     *
     * @param cookieName
     * @param cookieValue
     * @param maxAge
     * @param uriInfo
     * @param httpHeaders
     * @return
     */
    @Path("/cookie/cache/{name}/{value}/max-age-header/{age}")
    @GET
    @Timed
    public Response maxAgeCookie(@PathParam("name") String cookieName, @PathParam("value") String cookieValue, @PathParam("age") Integer maxAge, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
        String scheme = uriInfo.getRequestUri().getScheme();
        boolean isSecure = false;
        if ("https".equals(scheme)) {
            isSecure = true;
        }

        String requestCookieValue = "";

        Map<String, Cookie> cookieMap = httpHeaders.getCookies();
        if (cookieMap != null) {
            Cookie requestCookie = cookieMap.get(cookieName);
            if (requestCookie != null) {
                requestCookieValue = requestCookie.getValue();
            }
        }

        NewCookie cookie = new NewCookie(cookieName, cookieValue, "/api/cookie", uriInfo.getRequestUri().getHost(), "", maxAge, isSecure);

        Response.ResponseBuilder responseBuilder = Response.ok()
                .header("Cache-Control", "max-age=" + maxAge)
                .cookie(cookie)
                .entity(new ResponseMessage((maxAge > 0), Response.Status.OK.getStatusCode(), requestCookieValue));

        return responseBuilder.build();
    }

    private Response getConfigurableCacheableResponse(Integer maxAge) {

        Response.ResponseBuilder responseBuilder = Response.ok(new ResponseMessage((maxAge > 0), Response.Status.OK.getStatusCode()))
                .header("Cache-Control", "max-age=" + maxAge);

        return responseBuilder.build();
    }

    private void sleepForNumberSeconds(Integer responseTime) {
        if (responseTime < 60) {
            if (REMAINING_WAIT_LOCKS.decrementAndGet() > 0) {

                try {
                    Thread.sleep(responseTime * 1000);
                } catch (InterruptedException e) {
                    LOG.error("Problem sleeping for seconds: " + responseTime, e);
                }

            } else {
                throw new IllegalThreadStateException("Too many requests currently waiting");
            }
            REMAINING_WAIT_LOCKS.incrementAndGet();
        } else {
            throw new InvalidParameterException("Wait time must be below 60 seconds");
        }

    }
}