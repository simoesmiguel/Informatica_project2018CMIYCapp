package com.example.currentplacedetailsonmap;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.someoneelse.library.KeyExchange;
import com.example.someoneelse.library.LocationMethodsAndroid;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class QReader extends AppCompatActivity {

    private Button scan_btn;
    private Button generate;

    String TAG = "QReaderTAG";
    Activity activity = QReader.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 0;
    private String myMobileNumber;
    private String othersMobileNumber;
    private KeyExchange kex ;
    private boolean primesExist =false;
    private String encodedPk;
    private LocationMethodsAndroid lm;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qreader);
        lm = new LocationMethodsAndroid(this);
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        }
        if(lm.getMyPhoneNumber().equals("")){ // se o contacto ainda nao estiver guardado
            Log.d("Contato nao guardado","Contato nao guardado");
            /*if(!getPhone().equals("") && getPhone()!=null) { // se conseguir aceder ao contacto automaticamente
                Log.d("contacto automatico","conseguiu ir buscar o contato automaticamente");
                //Toast.makeText(activity,"phone number "+getPhone(), Toast.LENGTH_LONG).show();  // phone number added automatically
                lm.saveMyPhoneNumber(getPhone()); // save my phone number

            }else{*/
                Log.d("pedir ao utilizador","pedir o contacto ao utilizador");
                getMobileNumber();  // ask user for phone number and save it

            //}
        }else{
            myMobileNumber = lm.getMyPhoneNumber();
        }

        kex = new KeyExchange(myMobileNumber);

        scan_btn = (Button) findViewById(R.id.scan_btn);
        generate = (Button) findViewById(R.id.generateButton);

        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    generateQrCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    public void getMobileNumber(){ //faz um request à bd para obter o numero de telemovel

        // se nao existir, é necessario pedir ao utilizador o numero de telemovel
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mobile number is needed to generate your QR code");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myMobileNumber = input.getText().toString();
                Toast.makeText(getApplicationContext(),"Successfully added",Toast.LENGTH_SHORT).show();
                lm.saveMyPhoneNumber(myMobileNumber);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Reading Qrcode","Reading Qrcode");

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!= null){
            if(result.getContents()==null){
                Toast.makeText(this,"you cancelled the scanning",Toast.LENGTH_SHORT).show();
            }
            else{
                try {
                    kex.generateDhPublicKeyFromOther(result.getContents().split(",")[0]);
                    othersMobileNumber = result.getContents().split(",")[1]; //save the other person's mobile number
                    if(primesExist == false){  // so o utilizador que ainda nao gerou nenhum QR code vai entrar aqui dentro

                        Log.d("chave gerada","chave gerada");
                        kex.generateDhKeyPairFromOther();
                        kex.init();

                        encodedPk = kex.getEncodedPublicKey();
                        primesExist = true;
                        generateQrCode();
                    }

                    kex.firstPhase();
                    kex.generateSecret();
                    kex.generateSecretKey( kex.getSharedSecret());
                    Log.d("vou guardar","vou guardar");
                    saveToDatabase();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(this,"Contact added successfully", Toast.LENGTH_SHORT).show();  // guardar o nome da pessoa, o numero de telemovel e o segredo dessa pessoa (podem vir separados por virgulas p.ex)
                                                                                            // usar esse segredo para gerar um segredo partilhado
            }
        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveToDatabase() throws ClassNotFoundException {
        Log.d("vou guardar 2","vou guardar 2");
        lm.saveSecretKey(Integer.parseInt(othersMobileNumber),Base64.encodeToString(kex.getSecretKey().getEncoded(), Base64.NO_WRAP));
        lm.saveSecretKey(Integer.parseInt(myMobileNumber),Base64.encodeToString(kex.getSecretKey().getEncoded(), Base64.NO_WRAP));
    }


    public void generateQrCode() throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException, InvalidAlgorithmParameterException {

        if(primesExist == false){
            Log.d("aqui1","aqui1");
            encodedPk = generateKey();
        }

        Log.d("Qrcode gerado","QRcode gerado");

        ImageView imageView = (ImageView) findViewById(R.id.qrCode);
        try {
            Bitmap bitmap = encodeAsBitmap(encodedPk+","+myMobileNumber);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String generateKey() throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException, InvalidAlgorithmParameterException {
        Log.d("chave gerada","chave gerada");

        kex.generateDhKeyPair();
        kex.init();
        String encodedAlicePk = kex.getEncodedPublicKey();
        primesExist = true;
        //ler QR code da outra pessoa
        return encodedAlicePk;
    }


    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 180, 180, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 180, 0, 0, w, h);
        return bitmap;
    }


    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        return phoneMgr.getLine1Number();
    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone());
                    //Toast.makeText(activity,"phone number "+getPhone(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
