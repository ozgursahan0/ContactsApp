package com.ozgursahan.contactsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ozgursahan.contactsapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Contact> contactArrayList;
    ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        contactArrayList= new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactArrayList);
        binding.recyclerView.setAdapter(contactAdapter);

        getData();

        ListView listView = findViewById(R.id.listView);
        ArrayList<String> contactStringArrayList = new ArrayList<>(contactArrayList.size());
        for(Contact c : contactArrayList) {
            contactStringArrayList.add(Objects.toString(c.name, null));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactStringArrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,AddContactActivity.class);
                intent.putExtra("info","new");
                startActivity(intent);
            }
        });

    }

    public void getData () {
        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Contacts",MODE_PRIVATE,null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM contacts",null);
            int nameIndex = cursor.getColumnIndex("name");
            int idIndex = cursor.getColumnIndex("id");

            while (cursor.moveToNext())
            {
                String name=cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);
                Contact contact = new Contact(name,id);
                contactArrayList.add(contact);
            }

            contactAdapter.notifyDataSetChanged();
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_contact_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.add_contact) // birden fazla olabilir item
        {
            Intent intent = new Intent(MainActivity.this,AddContactActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}