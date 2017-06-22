package com.bignerdranch.android.audiceive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResult implements Serializable {
    private String fullUrl;
    private String thumbUrl;
    private String title;

    public String getFullUrl() {
        return fullUrl;
    }
    public String getThumbUrl() {
        return thumbUrl;
    }
    public String getTitle() {
        return title;
    }

    public SearchResult(JSONObject json){
        try {
            this.fullUrl = json.getString("link");
            this.title = json.getString("title");
            this.thumbUrl = json.getJSONObject("image").getString("thumbnailLink");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<SearchResult> fromJSONArray (JSONArray array){
        ArrayList<SearchResult> results = new ArrayList<>();
        for (int i=0; i < array.length(); i++){
            try {
                results.add(new SearchResult(array.getJSONObject(i)));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return results;
    }
}
