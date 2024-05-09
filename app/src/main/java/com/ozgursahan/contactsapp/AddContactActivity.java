package com.ozgursahan.contactsapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.ozgursahan.contactsapp.databinding.ActivityAddContactBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.SplittableRandom;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddContactBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Contacts",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("new"))
        {   // new contact
            binding.nameText.setText("");
            binding.numberText.setText("");
            binding.imageView.setImageResource(R.drawable.select);
            binding.saveButton.setVisibility(View.VISIBLE);
        }
        else if (info.matches("old"))
        {   // show contacts
            int contactId=intent.getIntExtra("contactId",0);
            binding.saveButton.setVisibility(View.INVISIBLE);

            try{
                Cursor cursor = database.rawQuery("SELECT * FROM contacts WHERE id = ?",new String[]{String.valueOf(contactId)}); // soru işareti yerine string dizisi gelir
                int nameIndex = cursor.getColumnIndex("name");
                int numberIndex = cursor.getColumnIndex("number");
                int imageIndex = cursor.getColumnIndex("image");

                while (cursor.moveToNext())
                {
                    binding.nameText.setText(cursor.getString(nameIndex));
                    binding.numberText.setText(cursor.getString(numberIndex));

                    byte[] bytes = cursor.getBlob(imageIndex);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                    binding.nameText.setEnabled(false);
                    binding.numberText.setEnabled(false);
                    binding.imageView.setEnabled(false);
                }
                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void save (View view)
    {
        String name=binding.nameText.getText().toString();
        String number=binding.numberText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS contacts(id INTEGER PRIMARY KEY, name VARCHAR, number VARCHAR, image BLOB)");

            String sqlString="INSERT INTO contacts(name,number,image) VALUES (?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);                                     // İLGİNÇ
            sqLiteStatement.bindString(2,number);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(AddContactActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // ÖNCEKİ AÇIK AKTİVİTELERİ KAPATIR
        startActivity(intent);

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){  // resimleri küçültmek için
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapOran = (float) width / (float) height;

        if(bitmapOran>=1)
        {
            //landscape image yatay
            width=maximumSize;
            height=(int) (width/bitmapOran);
        }
        else
        {
            //portrait image dikey
            height=maximumSize;
            width=(int) (height*bitmapOran);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void selectImage (View view)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Snackbar.make(view,"Permission Needed for Gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }
            else
            {
                // request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }
        else
        {
            //gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void registerLauncher() { // resultLauncherlar için ne yapacaklarını

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK)
                {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult!=null)
                    {
                        Uri imageData=intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData); //<--- yapılabilirdi ama biz bitmap kullanıcaz

                        // BİTMAP kullanımı
                        try {
                            if (Build.VERSION.SDK_INT >= 28)
                            {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                            else
                            {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result)
                {
                    //permission granted (izin verildi)
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else
                {
                    //permission denied
                    Toast.makeText(AddContactActivity.this,"Permission Needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}