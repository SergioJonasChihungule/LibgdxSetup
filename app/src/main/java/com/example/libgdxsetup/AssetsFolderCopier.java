package com.example.libgdxsetup;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AssetsFolderCopier {
    
    
    public static String ROOT_PATH = "/LibgdxProjects" ;
      public static boolean copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            // Verifica se o armazenamento externo está disponível
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
            
            // Cria o diretório de destino se não existir
            File dstFolder = new File(Environment.getExternalStorageDirectory(), ROOT_PATH +"/" + dstName);
            if (!dstFolder.exists()) {
                if (!dstFolder.mkdirs()) {
                    return false;
                }
            }
            
            // Lista todos os arquivos na pasta assets
            String[] files = context.getAssets().list(srcName);
            if (files == null || files.length == 0) {
                return false;
            }
            
            // Copia cada arquivo
            for (String file : files) {
                String srcPath = srcName + File.separator + file;
                String dstPath = dstName + File.separator + file;
                
                // Se for um diretório, chama recursivamente
                if (context.getAssets().list(srcPath).length > 0) {
                    copyAssetFolder(context, srcPath, dstPath);
                } else {
                    copyAssetFile(context, srcPath, dstPath);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }
    
   public static void saveBitmap(Bitmap bitmap, String path) {
    try {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
   public static Bitmap uriToBitmap(Uri uri, Context context) {
    try {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
        
        
}
   public static Bitmap getRoundedCornerBitmapR(Bitmap bitmap, float cornerRadius) {
    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Paint paint = new Paint();
    paint.setAntiAlias(true);

    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    final RectF rectF = new RectF(rect);

    Path path = new Path();
    path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);

    canvas.clipPath(path);
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
}
    
   public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius) {
    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    final RectF rectF = new RectF(rect);

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(Color.WHITE);
    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
}
    private static void copyAssetFile(Context context, String srcName, String dstName) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getAssets().open(srcName);
            File outFile = new File(Environment.getExternalStorageDirectory(), ROOT_PATH +"/" + dstName);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
    
   public static void saveBitmapToFile(Bitmap bitmap, File file) {
    try {
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    
  public static void copyFile(File origem, File destino) throws IOException {
    FileInputStream in = new FileInputStream(origem);
    FileOutputStream out = new FileOutputStream(destino);

    byte[] buffer = new byte[1024];
    int length;
    while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
    }

    in.close();
    out.close();
}
    
   public static String getRealPathFromUri(Context context, Uri uri) {
    String[] projection = {MediaStore.Images.Media.DATA};
    Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
    if (cursor != null) {
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }
    return null;
}

    public static List<String> readLines(Context context, String path) {
        List<String> list = new ArrayList<>();
        try  (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(path), "UTF-8"))){
            String linha; 
           while((linha = reader.readLine()) != null){
               list.add(linha);
           }
         return  list;
        } catch(Exception err) {
        	err.printStackTrace();
        }
      return list;
    	
    }
    public static void readAndWrite(Context context, String assetFileName, 
                                  String outputFilePath, String replacementText, String minsdk) throws IOException {
        
        
        try (InputStream inputStream = context.getAssets().open(assetFileName);
             Reader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            
            // Lê todas as linhas do arquivo
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            
            String fileContent = contentBuilder.toString();
            
            fileContent = fileContent.replace("$projectname$", replacementText);
           
            fileContent = fileContent.replace("$packagename$", replacementText);
               
            
            
            if (fileContent.contains("$orientation$")) {
                fileContent = fileContent.replace("$orientation$", minsdk);
                
            } 
            if(minsdk != null){
               fileContent = fileContent.replace("$minsdk$", minsdk );
            }
           FileCreator.criarArquivo(context, outputFilePath, fileContent);
            
            // Chama o método para criar o arquivo com o conteúdo modificado
            
        } catch (IOException e) {
            e.printStackTrace();
            throw e; // Re-lança a exceção após logar
        }
    }
   
}

// Classe auxiliar para criação de arquivos
    class FileCreator {
    private static FileWriter fileWriter;
    
    public static void criarArquivo(Context context, String filePath, String content) {
        try {
            File file = new File(filePath);
            
            // Verifica se o arquivo não existe
            if (!file.exists()) {
                // Cria os diretórios pais se necessário
                file.getParentFile().mkdirs();
                
                // Cria o FileWriter em modo append (true)
                fileWriter = new FileWriter(file, true);
                
                // Escreve o conteúdo e faz flush
                fileWriter.write(content);
                fileWriter.flush();
                
            }
        } catch (Exception e) {
            // Tratamento de erro com mensagem para o usuário
            String errorMessage = "Ocorreu um erro ao criar e salvar a classe.\n" + e.getMessage();
            Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            // Garante que o FileWriter seja fechado
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
   
    
    
}
    
