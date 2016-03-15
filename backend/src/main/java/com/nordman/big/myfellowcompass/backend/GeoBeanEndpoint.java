package com.nordman.big.myfellowcompass.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "geoBeanApi",
        version = "v2",
        resource = "geoBean",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myfellowcompass.big.nordman.com",
                ownerName = "backend.myfellowcompass.big.nordman.com",
                packagePath = ""
        )
)
public class GeoBeanEndpoint {

    private static final Logger logger = Logger.getLogger(GeoBeanEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(GeoBean.class);
    }

    /**
     * Returns the {@link GeoBean} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code GeoBean} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "geoBean/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public GeoBean get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting GeoBean with ID: " + id);
        GeoBean geoBean = ofy().load().type(GeoBean.class).id(id).now();
        if (geoBean == null) {
            throw new NotFoundException("Could not find GeoBean with ID: " + id);
        }
        return geoBean;
    }

    /**
     * Inserts a new {@code GeoBean}.
     */
    @ApiMethod(
            name = "insert",
            path = "geoBean",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public GeoBean insert(GeoBean geoBean) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that geoBean.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(geoBean).now();
        logger.info("Created GeoBean.");

        return ofy().load().entity(geoBean).now();
    }

    /**
     * Updates an existing {@code GeoBean}.
     *
     * @param id      the ID of the entity to be updated
     * @param geoBean the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code GeoBean}
     */
    @ApiMethod(
            name = "update",
            path = "geoBean/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public GeoBean update(@Named("id") Long id, GeoBean geoBean) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(geoBean).now();
        logger.info("Updated GeoBean: " + geoBean);
        return ofy().load().entity(geoBean).now();
    }

    /**
     * Deletes the specified {@code GeoBean}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code GeoBean}
     */
    @ApiMethod(
            name = "remove",
            path = "geoBean/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(GeoBean.class).id(id).now();
        logger.info("Deleted GeoBean with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "geoBean",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<GeoBean> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<GeoBean> query = ofy().load().type(GeoBean.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<GeoBean> queryIterator = query.iterator();
        List<GeoBean> geoBeanList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            geoBeanList.add(queryIterator.next());
        }
        return CollectionResponse.<GeoBean>builder().setItems(geoBeanList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(GeoBean.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find GeoBean with ID: " + id);
        }
    }

    @ApiMethod(
            name = "sayHi",
            path = "geoBean/{name}"
    )
    public GeoBean sayHi(@Named("name") String name) {
        GeoBean response = new GeoBean();
        response.setExtra("Hi, " + name);

        return response;
    }

}