package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.EdgeContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 * Series queries
 */

public interface MediaModel {

    @POST("/")
    @GraphQuery("MediaBase")
    @Headers("Content-Type: application/json")
    Call<MediaBase> getMediaBase(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaOverview")
    @Headers("Content-Type: application/json")
    Call<Media> getMediaOverview(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaRelations")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<String, MediaBase>>> getMediaRelations(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaStats")
    @Headers("Content-Type: application/json")
    Call<Media> getMediaStats(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaCharacters")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<String, CharacterBase>>> getMediaCharacters(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaStaff")
    @Headers("Content-Type: application/json")
    Call<ConnectionContainer<EdgeContainer<String, StaffBase>>> getMediaStaff(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("MediaSocial")
    @Headers("Content-Type: application/json")
    Call<PageContainer<FeedList>> getMediaSocial(@Body GraphQueryContainer request);
}