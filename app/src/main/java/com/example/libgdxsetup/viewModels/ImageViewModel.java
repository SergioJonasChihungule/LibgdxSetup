package com.example.libgdxsetup.viewModels;
import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class ImageViewModel extends ViewModel {
    private MutableLiveData<Bitmap> bitmap = new MutableLiveData<>();
    private MutableLiveData<List<String>> versions = new MutableLiveData<>();
    private MutableLiveData<String> orientation = new MutableLiveData<>();
     public  LiveData<Bitmap> getBitmap(){
         return bitmap;
     }
     public void setBitmap(Bitmap img){
         bitmap.setValue(img);
     }
     public  LiveData<String> getOrientation(){
         return orientation;
     }
     public void setOrientation(String orientation){
         this.orientation.setValue(orientation);
     }
 public  LiveData<List<String>> getVersions(){
         return versions;
     }
     public void setVersions(List<String> list){
         
         versions.setValue(list);
     }
}
