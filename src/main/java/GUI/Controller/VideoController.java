package GUI.Controller;

import GUI.Model.Content;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VideoController {
    @FXML
    private ImageView iView;
    
    VideoController(){
        Content.videoController = this;
    }

    public void updateImage(Image img){
        iView.setImage(img);
    }
}
