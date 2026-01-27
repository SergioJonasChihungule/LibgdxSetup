package com.example.libgdxsetup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.renderscript.Short2;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.RoundedCorner;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.libgdxsetup.viewModels.ImageViewModel;
import com.google.android.material.color.DynamicColors;
import java.io.IOException;
import android.content.res.AssetManager;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.libgdxsetup.databinding.ActivityMainBinding;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private String defaultPackageName = "com.game.";
  Bitmap img = null;
  private static String orientation = "portrait";
  ImageViewModel viewModelIMG;
  private Uri tempCropUri;
    
    

  // 1. Registra o seletor de galeria
ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri galleryUri = result.getData().getData();
            
            // COPIA a imagem da galeria para o seu cache interno
            Uri internalUri = copyUriToCache(galleryUri);
            
            if (internalUri != null) {
                launchCrop(internalUri); 
            }
        }
    }
);
    private Uri copyUriToCache(Uri sourceUri) {
    try {
        File tempFile = new File(getExternalCacheDir(), "input_for_crop.jpg");
        InputStream is = getContentResolver().openInputStream(sourceUri);
        OutputStream os = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
        os.close();
        is.close();
        // Usa o FileProvider que configuramos no passo anterior
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


// 2. Registra o Crop
ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Bundle extras = result.getData().getExtras();
            if (extras != null) {
                // O app de Crop devolve o bitmap com a chave "data"
                Bitmap b = (Bitmap) extras.get("data");
                
                if (b != null) {
                    this.img = AssetsFolderCopier.getRoundedCornerBitmapR(b, b.getWidth() * 0.20f);
                    viewModelIMG.setBitmap(this.img);
                }
            }
        } else {
            // Se o return-data falhar (raro), o Toast de acesso negado não aparecerá, 
            // mas o result será cancelado.
        }
    }
);


private void launchCrop(Uri sourceUri) {
    Intent intent = new Intent("com.android.camera.action.CROP");
    intent.setDataAndType(sourceUri, "image/*");
    
    // Concede permissão de leitura para a imagem que você copiou para o cache
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    
    intent.putExtra("crop", "true");
    intent.putExtra("aspectX", 1);
    intent.putExtra("aspectY", 1);
    
    // IMPORTANTE: Remova a linha intent.putExtra("output", ...) 
    // E adicione esta:
    intent.putExtra("return-data", true); 

    cropLauncher.launch(intent);
}


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Inflate and get instance of binding
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    // set content view to binding's root
    DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
    setContentView(binding.getRoot());

    viewModelIMG = new ViewModelProvider(this).get(ImageViewModel.class);
    if (viewModelIMG.getVersions().getValue() == null
        || viewModelIMG.getVersions().getValue().isEmpty()) {
      viewModelIMG.setVersions(AssetsFolderCopier.readLines(this, "sdkVersion.txt"));
    }

    viewModelIMG
        .getOrientation()
        .observe(
            this,
            orientation -> {
              this.orientation = orientation;
            });
    viewModelIMG
        .getBitmap()
        .observe(
            this,
            bitmap -> {
              Glide.with(this).load(bitmap).into(binding.logo);
            });
    viewModelIMG
        .getVersions()
        .observe(
            this,
            list -> {
              ArrayAdapter<String> adapter =
                  new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
              binding.autoComplete.setAdapter(adapter);
              binding.autoComplete.setText(list.get(8), false);
            });

    binding.copy.setOnClickListener(
        v -> {
          if (!Environment.isExternalStorageManager()) {
            requestPermission();
          } else {
            binding.logger.setVisibility(View.VISIBLE);
            if (!binding.projectname.getText().toString().isBlank()
                && !binding.packagename.getText().toString().isBlank()) {
              binding.logger.setText("Generating project...");
              
              
              generateProject(binding.configsRadio.getCheckedRadioButtonId());
            } else {
              if (binding.projectname.getText().toString().isBlank()) {
                binding.projectname.setError("É obrigatório preencher o campo");
              }
              if (binding.packagename.getText().toString().isBlank()) {
                binding.packagename.setError("É obrigatório preencher o campo");
              }
            }
          }
        });
    binding.logo.setOnClickListener(v -> {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    galleryLauncher.launch(intent); // Usa o novo launcher
});

    binding.projectname.addTextChangedListener(
        new TextWatcher() {

          @Override
          public void afterTextChanged(Editable arg0) {}

          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

          @Override
          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (binding.packagename.getText().toString().startsWith(defaultPackageName)) {
              binding.packagename.setText(
                  defaultPackageName
                      + binding.projectname.getText().toString().toLowerCase().replace(" ", ""));
            }
          }
        });

    binding.portrait.setOnClickListener(
        v -> {
          viewModelIMG.setOrientation("portrait");
          binding.sensorPortrait.setChecked(false);
          binding.landscape.setChecked(false);
          binding.sensorLandscape.setChecked(false);
          binding.sensor.setChecked(false);
        });
    binding.sensorPortrait.setOnClickListener(
        v -> {
          viewModelIMG.setOrientation("sensorPortrait");
          binding.portrait.setChecked(false);
          binding.landscape.setChecked(false);
          binding.sensorLandscape.setChecked(false);
          binding.sensor.setChecked(false);
        });

    binding.landscape.setOnClickListener(
        v -> {
          viewModelIMG.setOrientation("landscape");
          binding.portrait.setChecked(false);
          binding.sensorPortrait.setChecked(false);
          binding.sensorLandscape.setChecked(false);
          binding.sensor.setChecked(false);
        });

    binding.sensorLandscape.setOnClickListener(
        v -> {
          viewModelIMG.setOrientation("sensorLandscape");
          binding.portrait.setChecked(false);
          binding.sensorPortrait.setChecked(false);
          binding.landscape.setChecked(false);
          binding.sensor.setChecked(false);
        });

    binding.sensor.setOnClickListener(
        v -> {
          viewModelIMG.setOrientation("sensor");
          binding.portrait.setChecked(false);
          binding.sensorPortrait.setChecked(false);
          binding.landscape.setChecked(false);
          binding.sensorLandscape.setChecked(false);
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.binding = null;
    if (img != null) {}
  }

  public static void createFiles(
      Context context, String packagename, String projectname, int checkedId, String minsdk) {
    try {
      String project_root =
          Environment.getExternalStorageDirectory()
              + "/"
              + AssetsFolderCopier.ROOT_PATH
              + "/"
              + projectname;
              
              
      String gameout =
          project_root + "/core/src/main/java/" + packagename.replace(".", "/") + "/MyGame.java";
      String firstScreenout =
          project_root
              + "/core/src/main/java/"
              + packagename.replace(".", "/")
              + "/screens/FirstScreen.java";
      String buildout = project_root + "/build.gradle.kts";
      String gradlegameout = project_root + "/core/build.gradle";
      
      String gradleappout = project_root + "/android/build.gradle.kts";
      String stringsout = project_root + "/android/res/values/strings.xml";
      String launcherout =
          project_root
              + "/android/src/main/java/"
              + packagename.replace(".", "/")
              + "/AndroidLauncher.java";
      String settingsout = project_root + "/settings.gradle.kts";
String manifestout = project_root + "/android/AndroidManifest.xml";
  AssetsFolderCopier.readAndWrite(
          context, "files/AndroidManifest.xml", manifestout, projectname, orientation);
      
      // File f = new File(outputFilePath);

      if (checkedId == R.id.applisten) {
        AssetsFolderCopier.readAndWrite(context, "files/MyGame.java", gameout, packagename, null);
      }
      if (checkedId == R.id.game) {
        AssetsFolderCopier.readAndWrite(context, "files/MyGame2.java", gameout, packagename, null);
        AssetsFolderCopier.readAndWrite(
            context, "files/FirstScreen.java", firstScreenout, packagename, null);
      }

      AssetsFolderCopier.readAndWrite(
          context, "files/build.gradle1.kts", buildout, projectname, null);
      AssetsFolderCopier.readAndWrite(
          context, "files/LaunchActivity.java", launcherout, packagename, null);
      AssetsFolderCopier.readAndWrite(context, "files/strings.xml", stringsout, projectname, null);
    AssetsFolderCopier.readAndWrite(
          context, "files/build.gradle", gradlegameout, projectname, null);
      AssetsFolderCopier.readAndWrite(
          context, "files/build.gradle.kts", gradleappout, packagename, getMinsdk(minsdk));
      AssetsFolderCopier.readAndWrite(
          context, "files/settings.gradle.kts", settingsout, projectname, null);

    } catch (Exception err) {

    }
  }

  public void generateProject(int checkedId) {
    String projectname = binding.projectname.getText().toString();
    String packagename = binding.packagename.getText().toString();

    
    boolean success =
        AssetsFolderCopier.copyAssetFolder(getApplicationContext(), "Libgdx", projectname);

    if (success) {
      // Cópia bem-sucedida
      
     
    File noMedia = new File(Environment.getExternalStorageDirectory() + "/" + AssetsFolderCopier.ROOT_PATH,  ".nomedia");
    try {
    	if(!noMedia.exists()) noMedia.createNewFile();
    } catch(Exception err) {
    	err.printStackTrace();
    }
      createFiles(
          this, packagename, projectname, checkedId, binding.autoComplete.getText().toString());
      Toast.makeText(
              this,
              "Projecto criado em "
                  + Environment.getExternalStorageDirectory()
                  + AssetsFolderCopier.ROOT_PATH
                  + "/"
                  + projectname,
              Toast.LENGTH_LONG)
          .show();
      String project_root =
          Environment.getExternalStorageDirectory()
              + "/"
              + AssetsFolderCopier.ROOT_PATH
              + "/"
              + projectname;
      String out = project_root + "/android/res/drawable/ic_launcher.png";
      String out2 = project_root + "/android/res/drawable-v24/ic_launcher.png";

      if (img != null) {
        try {
          AssetsFolderCopier.saveBitmap(img, out);
          AssetsFolderCopier.saveBitmap(img, out2);

        } catch (Exception err) {
          Toast.makeText(this, "Imagem NÃO copiada \n     " + err.getMessage(), Toast.LENGTH_LONG)
              .show();
        }
      }

      binding.logger.setText(
          "Projecto criado em "
              + Environment.getExternalStorageDirectory()
              + AssetsFolderCopier.ROOT_PATH
              + "/"
              + projectname);

    } else {
      // Falha na cópia
      binding.logger.setText("Falha na criação do projecto");
    }
  }

  public void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        // intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
      }
    }
  }

  private static String getMinsdk(String str) {
    return str.substring(3, str.indexOf(":")).trim();
  }
}
