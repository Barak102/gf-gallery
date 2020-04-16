package com.gf.gallery;

import com.gf.gallery.domain.GoogleDriveDomain;
import com.gf.gallery.entities.GalleryImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/images")
public class ImageController {


    @Autowired
    private GoogleDriveDomain driveDomain;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<GalleryImage> Get() throws Exception {
        List<GalleryImage> galleryImages = new ArrayList<GalleryImage>();
        try {
            galleryImages = driveDomain.getImages();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return galleryImages;
    }
}
