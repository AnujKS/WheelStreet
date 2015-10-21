package com.example.diemen.mapapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anujkumars on 10/20/2015.
 */
public class SavedActivity extends AppCompatActivity {

    ArrayAdapter mAdapter;
    ListView listview;
    List<LocationModel> loclist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
        listview = (ListView) findViewById(R.id.list1);
        DbHandler db = new DbHandler(this);
        loclist = db.readLocData(this);
        final List<String> kList = new ArrayList<>();
        for (LocationModel key : loclist) {
            kList.add(key.name);
        }

        mAdapter = new ArrayAdapter<String>(SavedActivity.this, android.R.layout.simple_list_item_1, kList);
        listview.setAdapter(mAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get the selected Location
                LocationModel locationModel = loclist.get(position);
                Intent output = new Intent();

                //Send it back
                output.putExtra("latitude", locationModel.lat + "");
                output.putExtra("longitude", locationModel.lng + "");
                output.putExtra("name", locationModel.name + "");
                output.putExtra("address", locationModel.address + "");
                setResult(RESULT_OK, output);
                finish();

            }
        });
    }

}
