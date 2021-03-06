package com.bignerdranch.android.audiceive;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchImageFragment extends Fragment {

    public static SearchImageFragment newInstance() {
        return new SearchImageFragment();
    }

    private static int MAX_PAGE = 5;
    EditText etQuery;
    GridView gvResults;
    Button btnSearch;
    ArrayList<SearchResult> searchResults;
    SearchResultArrayAdapter imageAdapter;
    ImageSearchClient client;
    int startPage = 1;
    String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_images, container, false);
        etQuery = (EditText) view.findViewById(R.id.etQuery);
        gvResults = (GridView) view.findViewById(R.id.gvResults);
        btnSearch = (Button) view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                onImageSearch(1);
            }
        });

        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), DisplayImage.class);
                SearchResult searchResult = searchResults.get(position);
                i.putExtra("result", searchResult);
                startActivity(i);
            }
        });

        gvResults.setOnScrollListener(new ImageEndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (page <= MAX_PAGE) {
                    onImageSearch((10*(page-1)) + 1);
                }
            }
        });

        searchResults = new ArrayList<>();
        imageAdapter = new SearchResultArrayAdapter(this.getActivity(), searchResults);
        gvResults.setAdapter(imageAdapter);
        return view;
    }

    public void onImageSearch(int start) {

        if (isNetworkAvailable()) {
            client = new ImageSearchClient();
            query = etQuery.getText().toString();
            startPage = start;
            if (startPage == 1)
                imageAdapter.clear();

            if (!query.equals(""))
                client.getSearch(query, startPage, this.getActivity(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    JSONArray imageJsonResults;
                                    if (response != null) {
                                        imageJsonResults = response.getJSONArray("items");
                                        imageAdapter.addAll(SearchResult.fromJSONArray(imageJsonResults));
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.invalid_data, Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
                                Toast.makeText(getActivity().getApplicationContext(), R.string.service_unavailable, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            else {
                Toast.makeText(this.getActivity(), R.string.invalid_query, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this.getActivity(),R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setString(CharSequence string) {
        etQuery.setText(string);
    }
}
