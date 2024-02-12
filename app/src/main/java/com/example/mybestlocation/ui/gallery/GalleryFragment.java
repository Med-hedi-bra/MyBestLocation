package com.example.mybestlocation.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.MainActivity;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.databinding.FragmentGalleryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    ArrayList<Position> data = new ArrayList<Position>();

    public GalleryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        final TextView textView = binding.textGallery;
        binding.download.setOnClickListener(v -> {
            Download t = new Download(getActivity());
            t.execute((Void[]) null);

        });

        return root;
    }
    class Download extends AsyncTask {
        Context con;
        AlertDialog alert;
Download(Context con){
    this.con=con;
}
        @Override
        protected Object doInBackground(Object[] objects) {

            String url = MainActivity.url+ "/get_all_user.php";
            Log.d("AsyncTask", "Fetching data from URL: " + url);


            JSONObject response = JSONParser.makeRequest(url);
            try {
                int sucess = response.getInt("success");
                if(sucess==0){
                    String msg = response.getString("message");
                }
                else {
                    JSONArray tableau = response.getJSONArray("UnePosition");
                    for(int i=0;i<tableau.length();i++){
                        JSONObject ligne = tableau.getJSONObject(i);
                        int id = ligne.getInt("id");
                        String longitude = ligne.getString("longitude");
                        String latitude = ligne.getString("latitude");
                        String description = ligne.getString("description");
                        data.add(new Position(id,longitude,latitude,description));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this.con);
            dialog.setTitle("Download...");
            dialog.setMessage("Please Wait");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            //EIT
            super.onPostExecute(o);
            ArrayAdapter ad = new ArrayAdapter(con, android.R.layout.simple_list_item_1,data);
            binding.listViewFavoris.setAdapter(ad);
            alert.dismiss();
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            //EIT
            super.onProgressUpdate(values);
        }
    }

}
